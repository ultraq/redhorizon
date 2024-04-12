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

package nz.net.ultraq.redhorizon.cli.converter

import nz.net.ultraq.redhorizon.classic.filetypes.CpsFileWriter
import nz.net.ultraq.redhorizon.filetypes.PcxFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Converter subcommand for converting PCX files to C&C CPS files.
 *
 * @author Emanuel Rabina
 */
@Command(
	name = 'pcx2cps',
	description = 'Convert a PCX file to a Command & Conquer CPS file'
)
class Pcx2CpsConverterCli implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(Pcx2CpsConverterCli)

	@Parameters(index = '0', description = 'A 320x200 PCX image.  If creating a paletted CPS, then the PCX file must have an internal palette.')
	File sourceFile

	@Parameters(index = '1', description = 'Path for the CPS file to be written to.')
	File destFile

	@Option(names = ['-o', '--overwrite'], description = 'Overwrite the destination file if it already exists')
	boolean overwrite

	/**
	 * Perform the file conversion.
	 */
	@Override
	Integer call() {

		logger.info('Loading {}...', sourceFile)
		if (sourceFile.exists()) {
			if (destFile.exists() && overwrite) {
				destFile.delete()
			}
			if (!destFile.exists()) {
				sourceFile.withInputStream { inputStream ->
					destFile.withOutputStream { outputStream ->
						def pcxFile = new PcxFile(inputStream)
						new CpsFileWriter(pcxFile).write(outputStream)
					}
				}
				return 0
			}
			else {
				logger.error('Output file, {}, already exists', destFile)
			}
		}
		else {
			logger.error('{} not found', sourceFile)
		}
		return 1
	}
}
