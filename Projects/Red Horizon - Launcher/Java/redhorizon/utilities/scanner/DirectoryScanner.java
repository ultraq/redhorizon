
package redhorizon.utilities.scanner;

import java.io.File;
import java.nio.file.Paths;

/**
 * Enumerates all files in a directory, calling any registered listeners
 * attached to this scanner when the file or directory matches a pattern set by
 * the listener.
 * 
 * @author Emanuel Rabina
 */
public class DirectoryScanner {

	private final File directory;
	private final ScannerListener[] listeners;

	/**
	 * Constructor, creates a new directory scanner which scans from the given
	 * directory.
	 * 
	 * @param directory Name of the directory to begin scanning from.
	 * @param listeners Listeners to attach to the scanner to be notified when
	 * 					they meet a file/directory that matches the listener's
	 * 					pattern.
	 * @throws IllegalArgumentException If <tt>directory</tt> doesn't actually
	 * 		   specify a directory.
	 */
	public DirectoryScanner(String directory, ScannerListener... listeners) {

		File checkdir = new File(directory);
		if (!checkdir.isDirectory()) {
			throw new IllegalArgumentException("Directory '" + directory + "' is not a directory");
		}

		this.directory = checkdir;
		this.listeners = listeners;
	}

	/**
	 * Starts a scan from the directory, notifying any registered listeners as
	 * necessary.
	 */
	public void scan() {

		scan(directory);
	}

	/**
	 * Scan the given directory, notifying any registered listeners as
	 * necessary.
	 * 
	 * @param dir
	 */
	private void scan(File dir) {

		File[] files = dir.listFiles();
		for (File file: files) {
			if (file.isDirectory()) {
				scan(file);
			}
			else {
				for (ScannerListener listener: listeners) {
					if (listener.pattern().matcher(file.getName()).matches()) {
						listener.match(Paths.get(file.getAbsolutePath()));
					}
				}
			}
		}
	}
}
