/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.utilities.converter

import nz.net.ultraq.redhorizon.filetypes.cps.CpsFile
import nz.net.ultraq.redhorizon.filetypes.pcx.PcxFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * CLI tool for converting PCX files to C&C CPS files.
 * 
 * @author Emanuel Rabina
 */
@Command(
	name = 'mix',
	header = [
		'',
		'Red Horizon Utilities - PCX file to CPS file converter',
		'======================================================',
		''
	],
	mixinStandardHelpOptions = true,
	version = '${sys:redhorizon.version}'
)
class Pcx2CpsConverter implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(Pcx2CpsConverter)

	@Spec
	CommandSpec commandSpec

	@Parameters(index = '0', arity = '1', description = 'A 320x200 PCX image.  If creating a paletted CPS, then the PCX file must have an internal palette.')
	File sourceFile

	@Parameters(index = '1', arity = '2', description = 'Path for the CPS file to be written to.')
	File destFile

	/**
	 * Perform the file conversion.
	 * 
	 * @return
	 */
	@Override
	Integer call() {

		logger.info('Red Horizon Utilities - PCX -> CPS file converter {}', commandSpec.version()[0] ?: '(development)')

		logger.info('Loading {}...', sourceFile)
		if (sourceFile.exists()) {
			def pcxFile = sourceFile.withInputStream { inputStream ->
				return new PcxFile(inputStream)
			}
			if (!destFile.exists()) {
				destFile.withOutputStream { outputStream ->
					new CpsFile(pcxFile).writeTo(outputStream)
				}
			}
			else {
				logger.error('Output file, {}, already exists', destFile)
			}
		}
		else {
			logger.error('{} not found')
			return 1
		}
		return 0
	}

	/**
	 * Bootstrap the application using Picocli.
	 * 
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(new CommandLine(new Pcx2CpsConverter()).execute(args))
	}
}
