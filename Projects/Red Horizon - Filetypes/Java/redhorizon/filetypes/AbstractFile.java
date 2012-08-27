
package redhorizon.filetypes;

/**
 * Abstract class for all file types.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractFile implements File {

	protected final String filename;

	/**
	 * Constructor, assigns this file a name.
	 * 
	 * @param filename Name of the file being represented by the class.
	 */
	protected AbstractFile(String filename) {

		this.filename = filename.contains(java.io.File.separator) ?
				filename.substring(filename.lastIndexOf(java.io.File.separator) + 1) : filename;
	}

	/**
	 * Returns whether the filenames of 2 <tt>FileImpl</tt>s
	 * match.  Unique filenames are a requirement of Red Horizon.
	 * 
	 * @param other The other file to compare against.
	 * @return <tt>true</tt> if the filenames match, <tt>false</tt>
	 * 		   otherwise.
	 */
	@Override
	public boolean equals(Object other) {

		return (other instanceof AbstractFile) && filename.equals(((AbstractFile)other).filename);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFileName() {

		return filename;
	}

	/**
	 * Hashcode algorithm for files.  It is simply the hashcode for the file's
	 * name, meaning that filenames must be unique across the game.  For the
	 * requirements of Red Horizon, this is sufficient.
	 * 
	 * @return The hashcode for the file's name string.
	 */
	@Override
	public int hashCode() {

		return filename.hashCode();
	}
}
