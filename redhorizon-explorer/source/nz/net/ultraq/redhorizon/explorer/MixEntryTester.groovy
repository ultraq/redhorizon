/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer

import nz.net.ultraq.redhorizon.classic.filetypes.AudFile
import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.filetypes.VqaFile
import nz.net.ultraq.redhorizon.filetypes.ResourceFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor

/**
 * Attempt to determine the type of a mix file entry by testing it against
 * various known file formats.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class MixEntryTester {

	private static final Logger logger = LoggerFactory.getLogger(MixEntryTester)

	// Any file in this list should only load the header data and lazily load their
	// main data so that testing can be fast
	private static final FileClasses = [VqaFile, ShpFile, AudFile] as List<Class<? extends ResourceFile>>

	final MixFile mixFile

	/**
	 * Attempt to determine the type of file this mix entry is for.
	 *
	 * @param mixEntry
	 * @return
	 *   A best guess of the class to use to load the entry, or {@code null} if
	 *   the type could not be determined.
	 */
	MixEntryTesterResult test(nz.net.ultraq.redhorizon.classic.filetypes.MixEntry mixEntry) {

		var hexId = Integer.toHexString(mixEntry.id)
		logger.debug('Attempting to determine type of entry w/ ID of {}', hexId)

		return mixFile.getEntryData(mixEntry).withBufferedStream { stream ->
			stream.mark(mixEntry.size)
			var result = FileClasses.inject(null) { acc, fileClass ->
				if (acc) {
					return acc
				}
				stream.reset()
				return testFileType(stream, fileClass, hexId)
			}
			if (!result) {
				logger.debug('Could not determine type')
			}
			return result
		}
	}

	/**
	 * Test the entry data as the given type.
	 *
	 * @return A result if the type matches the next entry, {@code null} otherwise.
	 */
	private static MixEntryTesterResult testFileType(BufferedInputStream stream, Class<? extends ResourceFile> fileClass, String hexId) {

		try {
			fileClass.newInstance(stream)
			logger.debug("Guessing ${fileClass.simpleName}")
			return new MixEntryTesterResult("(unknown, 0x${hexId})", fileClass)
		}
		catch (AssertionError ignored) {
			// Do nothing
		}

		return null
	}
}
