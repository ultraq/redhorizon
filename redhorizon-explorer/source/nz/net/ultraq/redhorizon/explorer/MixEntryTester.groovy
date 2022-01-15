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

import nz.net.ultraq.redhorizon.classic.filetypes.aud.AudFile
import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.shp.ShpFile
import nz.net.ultraq.redhorizon.classic.filetypes.vqa.VqaFile

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
	MixEntryTesterResult test(nz.net.ultraq.redhorizon.classic.filetypes.mix.MixEntry mixEntry) {

		def hexId = Integer.toHexString(mixEntry.id)
		logger.debug('Attempting to determine type of entry w/ ID of {}', hexId)

		// Try a VQA file
		def isVqaFile = VqaFile.test(mixFile.getEntryData(mixEntry))
		if (isVqaFile) {
			logger.debug('Guessing VQA file')
			return new MixEntryTesterResult(VqaFile, "(unknown VQA file, ID: 0x${hexId})")
		}

		// Try a SHP file
		def isShpFile = ShpFile.test(mixFile.getEntryData(mixEntry))
		if (isShpFile) {
			logger.debug('Guessing SHP file')
			return new MixEntryTesterResult(ShpFile, "(unknown SHP file, ID: 0x${hexId})")
		}

		// Try an AUD file
		def isAudFile = AudFile.test(mixFile.getEntryData(mixEntry))
		if (isAudFile) {
			logger.debug('Guessing AUD file')
			return new MixEntryTesterResult(AudFile, "(unknown AUD file, ID: 0x${hexId})")
		}

		logger.debug('Could not determine type')
		return null
	}
}
