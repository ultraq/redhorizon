
package redhorizon.resourcemanager;

import java.io.File;
import java.io.FileFilter;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic locator to find a file on the file system.  Maintains a list of files
 * within a collection of directories and subdirectories for fast finding.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractFileLocator implements ResourceLocator {

	private ArrayList<File> allfiles;

	/**
	 * Does nothing.
	 */
	@Override
	public final void close() {
	}

	/**
	 * Returns a list of directories to scan for files.
	 * 
	 * @return List of directories this locator searches for files in.
	 */
	protected abstract List<File> directories();

	/**
	 * Search for a file in the list of directories specified by this file
	 * locator.
	 * 
	 * @param name Name of the file to locate.
	 * @return The file with the matching file name, or <tt>null</tt> if the
	 * 		   file could not be found.
	 */
	@Override
	public FileChannel locate(String name) {

		if (allfiles == null) {
			allfiles = new ArrayList<>();
			for (File directory: directories()) {
				allfiles.addAll(scanDirectory(directory));
			}
			allfiles.trimToSize();
		}

		// Find and return the first match
		for (File file: allfiles) {
			if (file.getName().equals(name)) {
				return FileChannel.open(Paths.get(file.toURI()));
			}
		}
		return null;
	}

	/**
	 * Recursively scan a directory, returning all files found inside it.
	 * 
	 * @param root Parent folder at which to start the scan.
	 * @return List of files inside <tt>directory</tt> and its subdirectories.
	 */
	private ArrayList<File> scanDirectory(File root) {

		// Get files from subdirectories first
		File[] directories = root.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		ArrayList<File> files = new ArrayList<>();
		for (File directory: directories) {
			files.addAll(scanDirectory(directory));
		}

		// Add files in current directory
		files.addAll(Arrays.asList(root.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isDirectory();
			}
		})));

		return files;
	}
}
