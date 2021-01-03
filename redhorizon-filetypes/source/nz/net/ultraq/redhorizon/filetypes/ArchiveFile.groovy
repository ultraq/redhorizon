/* 
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes

/**
 * Interface for file formats which are holders of other files (eg: MIX, ZIP,
 * etc...).
 * 
 * @param <E> Archive file entry implementation.
 * @author Emanuel Rabina
 */
interface ArchiveFile<E extends ArchiveEntry> extends Closeable {

	/**
	 * Get the descriptor for an entry in the archive file.
	 * 
	 * @param name
	 *   Name of the entry as it exists within the archive file.  If the archive
	 *   file supports a directory structure within it, then this name can contain
	 *   a path structure.
	 * @return Descriptor of the entry, or {@code null} if the entry cannot be
	 *         found within the file.
	 */
	E getEntry(String name)

	/**
	 * Returns an input stream to the entry in the archive.
	 * 
	 * @param entry Descriptor of the entry in the archive file.
	 * @return An input stream for reading the entry within the archive.
	 */
	InputStream getEntryData(E entry)
}
