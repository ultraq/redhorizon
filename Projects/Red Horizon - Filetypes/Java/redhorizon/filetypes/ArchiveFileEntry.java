
package redhorizon.filetypes;

/**
 * Meta data on an entry found within an archive file.  Can be used to describe
 * a directory or file.
 * 
 * @author Emanuel Rabina
 */
public interface ArchiveFileEntry {

	/**
	 * Returns the name of the entry.
	 * 
	 * @return Entry name.
	 */
	public String getName();

	/**
	 * Return the size of the entry.
	 * 
	 * @return Size of the entry.
	 */
	public long getSize();
}
