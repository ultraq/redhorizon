
package redhorizon.utilities.scanner;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Interface for listener classes to be notified of directory scanning matches.
 * 
 * @author Emanuel Rabina
 * @see DirectoryScanner
 */
public interface ScannerListener {

	/**
	 * Called by a file system scanner when the file or directory it encounters
	 * matches the pattern specified by this listener.
	 * 
	 * @param path File/directory that matched the listener pattern.
	 * @see ScannerListener#pattern()
	 */
	public void match(Path path);

	/**
	 * Regular expression pattern that the file or directory name should match
	 * so that the listener gets notified.
	 * 
	 * @return File or directory name pattern.
	 */
	public Pattern pattern();
}
