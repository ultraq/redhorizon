/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.redhorizon.mixreader

import nz.net.ultraq.redhorizon.classic.filetypes.MixFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Read MIX files and extract their contents.
 *
 * @author Emanuel Rabina
 */
class MixReader implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(MixReader)

	private final MixFile mixFile

	MixReader(File file) {

		logger.info('Loading {}...', mixFile)
		mixFile = new MixFile(file)
	}

	@Override
	void close() {

		mixFile.close()
	}

	/**
	 * Extract the named item from the mix file, emitting it to the given output
	 * stream.
	 */
	void extract(String entryName, OutputStream outputStream) {

		var entry = mixFile.getEntry(entryName)
		if (entry) {
			logger.info('{} found, writing to file...', entryName)
			mixFile.getEntryData(entry).withBufferedStream { entryInputStream ->
				entryInputStream.transferTo(outputStream)
			}
		}
		else {
			logger.error('{} not found in {}', entryName, mixFile)
			throw new IllegalArgumentException()
		}
	}
}
