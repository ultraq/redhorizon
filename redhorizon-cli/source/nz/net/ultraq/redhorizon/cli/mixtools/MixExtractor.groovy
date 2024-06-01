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

package nz.net.ultraq.redhorizon.cli.mixtools

import nz.net.ultraq.redhorizon.classic.filetypes.MixFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * Write the data for a named entry in a MIX file, used primarily for testing
 * purposes.
 *
 * @author Emanuel Rabina
 */
@Command(
	name = 'extract',
	header = [
		'',
		'Red Horizon Mix Extractor',
		'=========================',
		''
	],
	description = 'Extract an entry from a mix file, saving it to disk with the same name.',
	mixinStandardHelpOptions = true
)
class MixExtractor implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(MixExtractor)

	@Spec
	CommandSpec commandSpec

	@Parameters(index = '0', description = 'Path to the mix file to read')
	String mixFile

	@Parameters(index = '1..*', description = 'Name of the entry/entries in the mix file')
	String[] entryNames

	/**
	 * Read from a MIX file the entry with the given name, writing it out to the
	 * same name on the file system.
	 */
	@Override
	Integer call() {

		logger.info('Red Horizon Mix Extractor {}', commandSpec.parent().parent().version()[0])

		println("Loading ${mixFile}...")
		new MixFile(new File(mixFile)).withCloseable { mix ->
			entryNames.each { entryName ->
				var entry = mix.getEntry(entryName)
				if (entry) {
					println("${entryName} found, writing to file...")
					mix.getEntryData(entry).withBufferedStream { entryInputStream ->
						new FileOutputStream(entryName).withCloseable { entryOutputStream ->
							entryInputStream.transferTo(entryOutputStream)
						}
					}
				}
				else {
					println("${entryName} not found in ${mixFile}")
					return 1
				}
			}
		}

		return 0
	}
}
