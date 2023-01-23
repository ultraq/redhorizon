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

package nz.net.ultraq.redhorizon.engine.resources

import nz.net.ultraq.redhorizon.filetypes.ArchiveFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions

import org.reflections.Reflections

import groovy.transform.Memoized
import groovy.transform.TupleConstructor

/**
 * The resource manager is used to search for files in directories and other
 * archives from some base directory, then loading them into a target type which
 * is saved for subsequent requests.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ResourceManager implements Closeable {

	@Lazy
	private Map<String, Class<? extends ArchiveFile>> knownArchiveTypes = {
		def archiveTypes = new Reflections(packageNames)
			.getSubTypesOf(ArchiveFile)
			.inject(new LinkedHashMap<String, Class<? extends ArchiveFile>>()) { acc, type ->
				def fileExtensionsAnnotation = type.getDeclaredAnnotation(FileExtensions)
				fileExtensionsAnnotation.value().each { extension ->
					acc << [(extension): type]
				}
				return acc
			}
		return archiveTypes
	}()

	final File baseDirectory
	final String[] packageNames

	private final Map<String, ArchiveFile> archiveFiles = [:]

	/**
	 * Close any archive files that have been opened during the life time of this
	 * resource manager.
	 */
	@Override
	void close() {

		archiveFiles.values()*.close()
	}

	/**
	 * Search for a file in the base directory, subdirectories, and any archive
	 * files encountered.  If found, an attempt is made to load it into the target
	 * class.
	 *
	 * @param resourceName
	 * @param targetType
	 * @return
	 */
	@Memoized
	<T> T loadFile(String resourceName, Class<T> targetType, File directory = baseDirectory) {

		return directory.listFiles().sort().findResult { file ->
			if (file.file) {
				def fileName = file.name
				if (fileName == resourceName) {
					return file.withInputStream { stream ->
						return targetType.newInstance(stream)
					}
				}
				def fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1)
				if (knownArchiveTypes.containsKey(fileExtension)) {
					def archiveType = knownArchiveTypes[fileExtension]
					def archive = archiveFiles.getOrCreate(file.path) { ->
						return archiveType.newInstance(file)
					}
					def entry = archive.getEntry(resourceName)
					if (entry) {
						return archive.getEntryData(entry).withBufferedStream { bis ->
							return targetType.newInstance(bis)
						}
					}
				}
				return null
			}
			return loadFile(resourceName, targetType, file)
		} as T
	}
}
