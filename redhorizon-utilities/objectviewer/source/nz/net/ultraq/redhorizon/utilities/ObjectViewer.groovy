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

package nz.net.ultraq.redhorizon.utilities

import nz.net.ultraq.redhorizon.classic.PaletteTypes
import nz.net.ultraq.redhorizon.classic.filetypes.ini.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.shp.ShpFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.utilities.objectviewer.MapViewer
import nz.net.ultraq.redhorizon.utilities.objectviewer.UnitViewer

import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * A game object viewer for testing their rendering and configuration.
 * 
 * @author Emanuel Rabina
 */
@Command(
	name = "view",
	header = [
		'',
		'Red Horizon Object Viewer',
		'=========================',
		''
	],
	mixinStandardHelpOptions = true,
	version = '${sys:redhorizon.version}'
)
class ObjectViewer implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(ObjectViewer)

	@Spec
	CommandSpec commandSpec

	@Parameters(index = '0', description = 'Path to the object file file to view, or a mix file that contains the object')
	File file

	@Parameters(index = '1', arity = '0..1', description = 'If <file> is a mix file, this is the name of the object in the mix file to view')
	String entryName

	@Option(names = ['--palette'], defaultValue = 'ra-temperate', description = 'Which game palette to apply to a paletted image.  One of "ra-snow", "ra-temperate", or "td-temperate".  Defaults to ra-temperate')
	PaletteTypes paletteType

	/**
	 * Launch the unit viewer.
	 *
	 * @return
	 */
	@Override
	Integer call() {

		Thread.currentThread().name = 'Object Viewer [main]'
		logger.info('Red Horizon Object Viewer {}', commandSpec.version()[0] ?: '(development)')

		logger.info('Loading {}...', this.file)
		def objectFile
		def objectId
		if (this.file.name.endsWith('.mix')) {
			new MixFile(this.file).withCloseable { mix ->
				def entry = mix.getEntry(entryName)
				if (entry) {
					logger.info('Loading {}...', entryName)
					objectFile = mix.getEntryData(entry).withStream { inputStream ->
						return getFileClass(entryName).newInstance(inputStream)
					}
					objectId = entryName[0..<-4]
				}
				else {
					logger.error('{} not found in {}', entryName, file)
					throw new IllegalArgumentException()
				}
			}
		}
		else {
			objectFile = this.file.withInputStream { inputStream ->
				return getFileClass(file.name).newInstance(inputStream)
			}
			objectId = this.file.name[0..<-4]
		}

		view(objectFile, objectId)

		return 0
	}

	/**
	 * Find the appropriate class for reading a file with the given name.
	 * 
	 * @param filename
	 * @return
	 */
	private Class<?> getFileClass(String filename) {

		def suffix = filename.substring(filename.lastIndexOf('.') + 1)
		def fileClass = new Reflections(
			'nz.net.ultraq.redhorizon.filetypes',
			'nz.net.ultraq.redhorizon.classic.filetypes'
		)
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().any { extension ->
					return extension.equalsIgnoreCase(suffix)
				}
			}
		if (!fileClass) {
			logger.error('No implementation for {} filetype', suffix)
			throw new IllegalArgumentException()
		}
		return fileClass
	}

	/**
	 * Display the unit.
	 * 
	 * @param objectFile
	 * @param objectId
	 */
	private void view(Object objectFile, String objectId) {

		switch (objectFile) {
			case ShpFile:
				new UnitViewer(objectFile, objectId, paletteType).view()
				break
			case IniFile:
				new MapViewer(objectFile).view()
				break
			default:
				logger.error('No viewer for the associated file class of {}', objectFile)
				throw new UnsupportedOperationException()
		}
	}

	/**
	 * Bootstrap the application using Picocli.
	 *
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(
			new CommandLine(new ObjectViewer())
				.registerConverter(PaletteTypes, { value ->
					return PaletteTypes.find { paletteType ->
						return value == paletteType.name().toLowerCase().replaceAll('_', '-')
					}
				})
				.setCaseInsensitiveEnumValuesAllowed(true)
				.execute(args)
		)
	}
}
