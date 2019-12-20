
// ===========================================
// Scanner's Java - File-related utility class
// ===========================================

package redhorizon.utilities;

import redhorizon.filetypes.ArchiveFile;

/**
 * This class deals with several file-handling tasks of Red Horizon, such as
 * copying and reading.<br>
 * <br>
 * The static methods of this class are for one-off functions like copying a
 * file, or writing some text/bytes to a file.  For functions with require
 * persistent use of a file, instead use the {@link FilePointer} utility class.
 * 
 * @author Emanuel Rabina
 */
public class FileUtility {

	/**
	 * Hidden default contructor, as this class is only ever meant to be used
	 * statically.
	 */
	private FileUtility() {
	}

	/**
	 * One-off operation, copies a file from one location to the other.
	 * 
	 * @param origpath Path to the original file.
	 * @param copypath Path of where to make a copy of the original.
	 */
	public static void copyFile(String origpath, String copypath) {

		FilePointer input = new FilePointer(origpath);
		input.copyTo(new FilePointer(copypath, false));
		input.close();
	}

	/**
	 * One-off operation, copies a file from the inside of a {@link
	 * redhorizon.filetypes.ArchiveFile} type, into the location specified.
	 * 
	 * @param origname	Name of the original file inside the container.
	 * @param container Name of the file holding the original file we're after.
	 * @param copypath	Path of where to make a copy of the original.
	 */
	public static void copyFileFromArchive(String origname, ArchiveFile container, String copypath) {

		FilePointer item = container.getItem(origname);
		item.copyTo(new FilePointer(copypath, false));
		item.close();
	}
}
