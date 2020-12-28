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

package nz.net.ultraq.redhorizon.utilities

import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
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
	name = 'mix',
	header = [
		'',
		'Red Horizon Mix Reader',
		'======================',
		''
	],
	description = 'Extract an entry from a mix file, saving it to disk with the same name.',
	mixinStandardHelpOptions = true,
	version = '${sys:redhorizon.version}'
)
class MixReader implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(MixReader)

	@Spec
	CommandSpec commandSpec

	@Parameters(index = '0', description = 'Path to the mix file to read')
	String mixFile

	@Parameters(index = '1', description = 'Name of the entry in the mix file')
	String entryName

	/**
	 * Read from a MIX file the entry with the given name, writing it out to the
	 * same name on the file system.
	 */
	@Override
	Integer call() {

		logger.info('Red Horizon Mix Reader {}', commandSpec.version()[0] ?: '(development)')

		logger.info('Loading {}...', mixFile)
		new MixFile(new File(mixFile)).withCloseable { mix ->
			def entry = mix.getEntry(entryName)
			if (entry) {
				logger.info('{} found, writing to file...', entryName)
				mix.getEntryData(entry).withCloseable { entryInputStream ->
					new FileOutputStream(entryName).withCloseable { entryOutputStream ->
						entryInputStream.transferTo(entryOutputStream)
					}
				}
			}
			else {
				logger.error('{} not found in {}', entryName, mixFile)
				throw new IllegalArgumentException()
			}
		}
	}

	/**
	 * Bootstrap the application using Picocli.
	 * 
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(new CommandLine(new MixReader()).execute(args))
	}
}
