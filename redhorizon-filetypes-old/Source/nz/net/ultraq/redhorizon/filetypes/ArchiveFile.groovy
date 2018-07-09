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

import java.nio.channels.ReadableByteChannel

/**
 * Interface for file formats which are holders of other files (eg: MIX, ZIP,
 * etc...).
 * 
 * @param <E> Archive file entry implementation.
 * @author Emanuel Rabina
 */
interface ArchiveFile<E extends ArchiveFileEntry> extends File {

	/**
	 * Get the descriptor for an entry in the archive file.
	 * 
	 * @param name Name of the entry as it exists within the archive file.  If
	 * 			   the archive file supports a directory structure within it,
	 * 			   then this name can be prefixed by a path structure.
	 * @return Descriptor of the entry, or <tt>null</tt> if the entry cannot be
	 * 		   found within the file.
	 */
	E getEntry(String name)

	/**
	 * Returns a byte channel to the entry in the archive.  Closing the archive
	 * will close all byte channels retrieved using this method.
	 * 
	 * @param entry Descriptor of the entry in the archive file.
	 * @return A byte channel bound to the entry within the archive.
	 */
	ReadableByteChannel getEntryData(E entry)
}
