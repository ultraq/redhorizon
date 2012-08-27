
package redhorizon.filetypes;

import java.nio.channels.ReadableByteChannel;

/**
 * Interface for file formats which are holders of other files (eg: MIX, ZIP,
 * etc...).
 * 
 * @param <E> Archive file entry implementation.
 * @author Emanuel Rabina
 */
public interface ArchiveFile<E extends ArchiveFileEntry> extends File {

	/**
	 * Get the descriptor for an entry in the archive file.
	 * 
	 * @param name Name of the entry as it exists within the archive file.  If
	 * 			   the archive file supports a directory structure within it,
	 * 			   then this name can be prefixed by a path structure.
	 * @return Descriptor of the entry, or <tt>null</tt> if the entry cannot be
	 * 		   found within the file.
	 */
	public E getEntry(String name);

	/**
	 * Returns a byte channel to the entry in the archive.  Closing the archive
	 * will close all byte channels retrieved using this method.
	 * 
	 * @param entry Descriptor of the entry in the archive file.
	 * @return A byte channel bound to the entry within the archive.
	 */
	public ReadableByteChannel getEntryData(E entry);
}
