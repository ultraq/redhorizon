/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli

import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile

import org.slf4j.Logger
import picocli.CommandLine.Parameters

/**
 * A mixin of shareable file options.
 * 
 * @author Emanuel Rabina
 */
class FileOptions {

	@Parameters(index = '0', description = 'Path to the file to open, or a mix file that contains the target object')
	File file

	@Parameters(index = '1', arity = '0..1', description = 'If <file> is a mix file, this is the name of the object in the mix file to open')
	File entryName

	/**
	 * Load the file indicated by the {@code file} and {@code entryName}
	 * parameters, passing it along to the given closure.
	 * 
	 * @param logger
	 * @param closure
	 */
	protected void useFile(Logger logger, Closure closure) {

		logger.info('Loading {}...', file)

		def fileClassForFileName = { String name, InputStream inputStream ->
			def fileClass = name.getFileClass().newInstance(inputStream)
			if (!fileClass) {
				logger.error('No implementation for {}', file.name)
				throw new IllegalArgumentException()
			}
			closure(fileClass)
		}

		if (file.name.endsWith('.mix')) {
			new MixFile(file).withCloseable { mix ->
				def entry = mix.getEntry(entryName.name)
				if (entry) {
					logger.info('Loading {}...', entryName)
					mix.getEntryData(entry).withBufferedStream { inputStream ->
						fileClassForFileName(entryName.name, inputStream)
					}
				}
				else {
					logger.error('{} not found in {}', entryName.name, file)
					throw new IllegalArgumentException()
				}
			}
		}
		else {
			file.withInputStream { inputStream ->
				fileClassForFileName(file.name, inputStream)
			}
		}
	}
}
