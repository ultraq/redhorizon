
package redhorizon.resourcemanager.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Enumerates all files in a directory and its subdirectories, calling any
 * registered listeners attached to this scanner when a file or directory
 * matches a pattern set by the listener.
 * 
 * @author Emanuel Rabina
 * @see ScannerListener
 */
public class DirectoryScanner {

	private static final Logger logger = LoggerFactory.getLogger(DirectoryScanner.class);

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

		logger.info("Scanning files in directory {}", dir.getAbsolutePath());
		File[] files = dir.listFiles();

		for (File file: files) {
			logger.info("Encountered file/dir {}", file.getName());
			if (file.isDirectory()) {
				scan(file);
			}
			else {
				for (ScannerListener listener: listeners) {
					Pattern pattern = listener.pattern();
					if (pattern.matcher(file.getName()).matches()) {
						logger.info("{} pattern {} matches file name, invoking listener.",
								listener.getClass().getSimpleName(), pattern);
						listener.match(Paths.get(file.getAbsolutePath()));
					}
				}
			}
		}
	}
}
