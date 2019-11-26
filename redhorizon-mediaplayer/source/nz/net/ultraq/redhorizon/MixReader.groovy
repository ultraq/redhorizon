/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.filetypes.mix.MixFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Write the data for a named entry in a MIX file, used primarily for testing
 * purposes.
 * 
 * @author Emanuel Rabina
 */
class MixReader {

	private static final Logger logger = LoggerFactory.getLogger(MixReader)

	/**
	 * Read from a MIX file the entry with the given name, writing it out to the
	 * same name on the file system.
	 * 
	 * @param args
	 */
	static void main(String[] args) {

		try {
			if (args.length != 2) {
				throw new IllegalArgumentException('Path to mix file and name of entry required')
			}

			def (pathToMixFile, entryName) = args
			def mixFile = new MixFile(new File(pathToMixFile))
			mixFile.withCloseable { mix ->
				def entry = mix.getEntry(entryName)
				if (entry) {
					mix.getEntryData(entry).withCloseable { entryInputStream ->
						new FileOutputStream(entryName).withCloseable { entryOutputStream ->
							entryInputStream.transferTo(entryOutputStream)
						}
					}
				}
				else {
					logger.error("${entryName} not found in ${pathToMixFile}")
					System.exit(1)
				}
			}
		}
		catch (Exception ex) {
			logger.error(ex.message, ex)
			System.exit(1)
		}
	}
}
