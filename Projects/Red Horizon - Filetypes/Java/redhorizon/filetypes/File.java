
package redhorizon.filetypes;

import java.io.Closeable;

/**
 * Top-level type for all files.
 * 
 * @author Emanuel Rabina
 */
public interface File extends Closeable {

	/**
	 * Returns the name of the file.
	 * 
	 * @return The file name.
	 */
	public String getFileName();
}
