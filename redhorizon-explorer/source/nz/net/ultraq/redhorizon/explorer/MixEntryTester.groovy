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

	private final Logger logger = LoggerFactory.getLogger(MixEntryTester)

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

		return mixFile.getEntryData(mixEntry).withBufferedStream { bufferedInputStream ->

			// Try a VQA file
			bufferedInputStream.mark(mixEntry.size)
			try {
				var vqaFile = new VqaFile(bufferedInputStream)
				logger.debug('Guessing VQA file')
				return new MixEntryTesterResult(vqaFile, "(unknown, 0x${hexId})", VqaFile)
			}
			catch (AssertionError ignored) {
			}

			// Try a SHP file
			bufferedInputStream.reset()
			try {
				var shpFile = new ShpFile(mixFile.getEntryData(mixEntry))
				logger.debug('Guessing SHP file')
				return new MixEntryTesterResult(shpFile, "(unknown, 0x${hexId})", ShpFile)
			}
			catch (AssertionError ignored) {
			}

			// Try an AUD file
			bufferedInputStream.reset()
			try {
				var audFile = new AudFile(mixFile.getEntryData(mixEntry))
				logger.debug('Guessing AUD file')
				return new MixEntryTesterResult(audFile, "(unknown, 0x${hexId})", AudFile)
			}
			catch (AssertionError ignored) {
			}

			logger.debug('Could not determine type')
			return null
		}
	}
}
