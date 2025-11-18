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
import nz.net.ultraq.redhorizon.filetypes.ResourceFile

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
		var archiveTypes = new Reflections(packageNames)
			.getSubTypesOf(ArchiveFile)
			.inject(new LinkedHashMap<String, Class<? extends ArchiveFile>>()) { acc, type ->
				var fileExtensionsAnnotation = type.getDeclaredAnnotation(FileExtensions)
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
	private final List<InputStream> inputStreams = []
	private File customDirectory

	/**
	 * Close any archive files that have been opened during the life time of this
	 * resource manager.
	 */
	@Override
	void close() {

		inputStreams*.close()
		archiveFiles.values()*.close()
	}

	/**
	 * If a search directory has been configured via {@link #withDirectory}, check
	 * that directory first for the resource, before falling back to searching the
	 * directory this resource manager was launched with.
	 */
	<T extends ResourceFile> T loadFile(String resourceName, Class<T> targetType) {

		if (customDirectory) {
			var result = loadFile(resourceName, targetType, customDirectory, false)
			if (result) {
				return result
			}
		}
		return loadFile(resourceName, targetType, baseDirectory, true)
	}

	/**
	 * Search for a file in the given directory and any subdirectories if {@code
	 * recursive} is {@code true}.  If found, an attempt is made to load it into
	 * the target class.
	 */
	@Memoized
	<T extends ResourceFile> T loadFile(String resourceName, Class<T> targetType, File directory, boolean recursive) {

		return directory.listFiles().sort().findResult { file ->
			if (file.file) {
				var fileName = file.name
				if (fileName == resourceName) {
					var inputStream = file.newInputStream()
					inputStreams << inputStream
					return targetType.newInstance(inputStream)
				}
				var fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1)
				if (knownArchiveTypes.containsKey(fileExtension)) {
					var archiveType = knownArchiveTypes[fileExtension]
					var archive = archiveFiles.getOrCreate(file.path) { ->
						return archiveType.newInstance(file)
					}
					var entry = archive.getEntry(resourceName)
					if (entry) {
						return targetType.newInstance(new BufferedInputStream(archive.getEntryData(entry)))
					}
				}
				return null
			}
			return recursive ? loadFile(resourceName, targetType, file, true) : null
		}
	}

	/**
	 * Use this directory as the basis for loading files with {@link #loadFile}
	 * without having to specify it all the time.  Only lasts for as long as the
	 * scope of the closure.
	 */
	<T> T withDirectory(File directory, Closure<T> closure) {

		try {
			customDirectory = directory
			return closure()
		}
		finally {
			customDirectory = null
		}
	}
}
