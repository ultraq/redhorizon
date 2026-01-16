/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer.extract

import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Given an entry in a mix file, save it elsewhere to disk.
 *
 * @author Emanuel Rabina
 */
class ExtractMixFileEntry {

	private static final Logger logger = LoggerFactory.getLogger(ExtractMixFileEntry)

	/**
	 * Save the data for a mix entry to disk.
	 */
	void extract(MixEntry entry, String name) {

		logger.info('Extracting {}...', name)
		new FileOutputStream(name).withCloseable { outputStream ->
			entry.mixFile().getEntryData(entry.mixEntry()).withBufferedStream { inputStream ->
				inputStream.transferTo(outputStream)
				logger.info('Done!')
			}
		}
	}
}
