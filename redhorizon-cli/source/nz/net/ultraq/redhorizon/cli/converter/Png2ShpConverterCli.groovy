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

import nz.net.ultraq.redhorizon.classic.filetypes.ShpFileWriter
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFileWriterOptions
import nz.net.ultraq.redhorizon.filetypes.PngFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Subcommand for converting PNG files to the C&C SHP format.
 *
 * @author Emanuel Rabina
 */
@Command(
	name = 'png2shp',
	description = 'Convert a paletted PNG file to a Command & Conquer SHP file'
)
class Png2ShpConverterCli implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(Png2ShpConverterCli)

	@Parameters(index = '0', description = 'The sounce PNG image.')
	File sourceFile

	@Parameters(index = '1', description = 'Path for the SHP file to be written to.')
	File destFile

	@Option(names = ['--width', '-w'], required = true, description = 'Width of each SHP image')
	int width

	@Option(names = ['--height', '-h'], required = true, description = 'Height of each SHP image')
	int height

	@Option(names = ['--numImages', '-n'], required = true, description = 'The number of images for the SHP file')
	int numImages

	/**
	 * Perform the file conversion.
	 */
	@Override
	Integer call() {

		logger.info('Loading {}...', sourceFile)
		if (sourceFile.exists()) {
			if (!destFile.exists()) {
				sourceFile.withInputStream { inputStream ->
					destFile.withOutputStream { outputStream ->
						def pngFile = new PngFile(inputStream)
						new ShpFileWriter(pngFile).write(outputStream, new ShpFileWriterOptions(width, height, numImages))
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
