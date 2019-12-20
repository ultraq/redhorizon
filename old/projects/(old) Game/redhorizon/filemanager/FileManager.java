
// ======================================
// Scanner's Java - Locates files on disk
// ======================================

package redhorizon.filemanager;

import redhorizon.filetypes.AnimationFile;
import redhorizon.filetypes.ArchiveFile;
import redhorizon.filetypes.BinaryFileBridge;
import redhorizon.filetypes.File;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.MapFile;
import redhorizon.filetypes.MissionFile;
import redhorizon.filetypes.PaletteFile;
import redhorizon.filetypes.SoundFile;
import redhorizon.filetypes.SoundTrackFile;
import redhorizon.filetypes.TextFileBridge;
import redhorizon.filetypes.VideoFile;
import redhorizon.strings.Strings;
import redhorizon.utilities.FilePointer;
import static redhorizon.filemanager.MetaTypes.*;

import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for providing methods to load files found on the
 * file system, and then storing links to those files for future use.  It acts
 * as a cache, and central point for the game to retrieve the files it requires.
 * <p>
 * When retrieving, files found outside of archives take precedence over those
 * found in archives.  This makes it easier to cater for custom content.
 * <p>
 * One thing to note, is that file requests to this class do not require a file
 * extension to be successful.  Instead, a search through all extensions that
 * are registered to have implemented the file type will be looked through until
 * a match is found (eg: request for sound file "Blah" will look for "Blah.aud",
 * "Blah.wav", "Blah.mp3", etc... for every known sound file extension).  The
 * order in which these extensions are searched is not guaranteed, so it would
 * be prudent for any mod makers to give each of their custom files a unique
 * name.
 * <p>
 * A call to {@link #init()} must be made before accessing this class, and a
 * call to {@link #close()} must be made afterwards to ensure that files are
 * closed, caches are cleared, etc.
 * 
 * @author Emanuel Rabina
 */
public class FileManager {

	// Location of the in-game files directory
	private static final String RESOURCE_DIR = "Resources";

	// Lists of core files
	private static final Map<String,File> filecache =
		Collections.synchronizedMap(new HashMap<String,File>());
	private static final List<ArchiveFile> archivecache =
		Collections.synchronizedList(new ArrayList<ArchiveFile>());

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private FileManager() {
	}

	/**
	 * Clears the internal cache of it's files, effectively freeing-up file
	 * links and memory space.
	 */
	public static void clear() {

		filecache.clear();
	}

	/**
	 * Closes the file manager by clearing all cached content, as well as making
	 * it impossible to cache any further files.
	 */
	public static void close() {

		archivecache.clear();
		filecache.clear();
	}

	/**
	 * Loads the given file to the appropriate type.
	 * 
	 * @param filetype The type of file to load.
	 * @param filename The name of the file.
	 * @return The right class for the given file.
	 */
	private static File findImplementationClass(FileTypes filetype, String filename) {

		return findImplementationClass(filetype, filename, new FilePointer(filename));
	}

	/**
	 * Loads the given file to the appropriate type.
	 * 
	 * @param filetype The type of file to load.
	 * @param filename The name of the file.
	 * @param pointer  <tt>FilePointer</tt> to the file.
	 * @return The right class for the given file.
	 */
	private static File findImplementationClass(FileTypes filetype, String filename, FilePointer pointer) {

		// Create the matching class
		Class<?> implementation = FileTypesOverride.hasMatchingType(filename) ?
				Class.forName(FileTypesOverride.getImplementation(filename)) :
				Class.forName(filetype.implementation);
		Constructor<?> constructor = implementation.getConstructor(String.class, FilePointer.class);
		return (File)constructor.newInstance(filename, pointer);
	}

	/**
	 * Retrieves the given item from the available content repositories and
	 * loads it into the appropriate <tt>File</tt> subclass.  Unlike the public
	 * methods of this class, this method required a file extension.
	 * 
	 * @param name The name of the item to retrieve.
	 * @param type The matching enumerated type.
	 * @return An implementation class wrapper for the item.
	 */
	private static File getFile(String name, FileTypes type) {

		// First check the file cache
		if (filecache.containsKey(name)) {
			return filecache.get(name);
		}

		// Otherwise, head for the archives
		ArchiveFile container = hasFileInArchive(name);
		if (container == null) {
			throw new MissingItemException(
					Strings.getText(FileManagerStringsKeys.MISSING_FILE, name));
		}
		return getFile(name, type, container);
	}

	/**
	 * Retrieves a file with the given name and type.
	 * 
	 * @param filename Name of the file containing the data.
	 * @param type	   Type (as taken from {@link MetaTypes}) of the data.
	 * @return Matching implementation class for the file, to be converted to
	 * 		   data.
	 */
	private static File getFile(String filename, int type) {

		// If a file extension is provided, search for the file immediately
		if (filename.contains(".")) {
			return getFile(filename, FileTypes.getMatchingType(filename, type));
		}

		// Go through all of the possible file extensions that have this type
		File file = null;
		for (FileTypes filetypes: FileTypes.values()) {
			if ((filetypes.type & type) == type) {
				for (String extension: filetypes.extensions) {
					try {
						file = getFile(filename + extension, filetypes);
						if (file != null) {
							break;
						}
					}
					catch (MissingItemException miex) {
						continue;
					}
				}
			}
		}

		// If still nothing found
		if (file == null) {
			throw new MissingItemException(
					Strings.getText(FileManagerStringsKeys.MISSING_FILE, filename));
		}

		return file;
	}

	/**
	 * The same as {@link #getFile(String, FileTypes)} except that it is known
	 * before-hand which archive to look into (assumming that the file is even
	 * in an archive).  This is the way to get around content that has the same
	 * name which may be contained in several different archives, eg: un-scaled
	 * infantry in Red Alert.
	 * <p>
	 * Use when you want to bypass cache access.  Since this method also caches
	 * any found items, it is also a good way to load the desired item, then
	 * make it available when calling the vanilla
	 * {@link #getFile(String, FileTypes)} method.
	 * 
	 * @param name		The name of the item to retrieve.
	 * @param type		The matching enumerated type.
	 * @param container The archive in which to find this item.
	 * @return An implementation class wrapper for the item.
	 */
	private static File getFile(String name, FileTypes type, ArchiveFile container) {

		// Find the file data in the archives
		ArchiveFile archive = archivecache.get(archivecache.indexOf(container));
		FilePointer pointer = archive.getItem(name);

		// Pass the file data
		return getFile(name, type, pointer);
	}

	/**
	 * Finds the appropriate <tt>File</tt> to use for loading the requested
	 * file.
	 * 
	 * @param name	  Name of the file (including extension).
	 * @param type	  The matching enumerated type.
	 * @param pointer <tt>FilePointer</tt> to the file data.
	 * @return Implementing class for the filename.
	 */
	private static File getFile(String name, FileTypes type, FilePointer pointer) {

		// Create the matching class, read the item from the file data
		File file = findImplementationClass(type, name, pointer);
		file.readFromFile();

		// Cache for future use
		filecache.put(name, file);

		return file;
	}

	/**
	 * Returns an <tt>AnimationFile</tt> implementation that has the given
	 * filename.
	 * 
	 * @param animationname The name of the animation file.
	 * @return <tt>AnimationFile</tt> implementation.
	 */
	public static AnimationFile getAnimationFile(String animationname) {

		return (AnimationFile)getFile(animationname, ANIMATION);
	}

	/**
	 * Returns the <tt>BinaryFileBridge</tt> class for any binary file type with
	 * the given name.
	 * 
	 * @param binaryname The name of the binary file.
	 * @return <tt>BinaryFileBridge</tt> class.
	 */
	public static BinaryFileBridge getBinaryFileBridge(String binaryname) {

		String binarykey = binaryname + "(BINARY)";

		// Check for existence in cache
		if (filecache.containsKey(binarykey)) {
			return (BinaryFileBridge)filecache.get(binarykey);
		}

		// Check in archives
		ArchiveFile archive = hasFileInArchive(binaryname);
		if (archive == null) {
			throw new MissingItemException(Strings.getText(FileManagerStringsKeys.MISSING_FILE, binaryname));
		}

		FilePointer filepointer = archive.getItem(binaryname);
		BinaryFileBridge binaryfile = new BinaryFileBridge(binaryname, filepointer);

		// Save to cache
		filecache.put(binarykey, binaryfile);

		return binaryfile;
	}

	/**
	 * Returns an <tt>ImageFile</tt> implementation that has the given filename.
	 * 
	 * @param imagename The name of the image file.
	 * @return <tt>ImageFile</tt> implmenetation.
	 */
	public static ImageFile getImageFile(String imagename) {

		return (ImageFile)getFile(imagename, IMAGE);
	}

	/**
	 * Returns an <tt>ImagesFile</tt> implementation that has the given
	 * filename.
	 * 
	 * @param imagename The name of the multi-image file.
	 * @return <tt>ImagesFile</tt> implementation.
	 */
	public static ImagesFile getImagesFile(String imagename) {

		return (ImagesFile)getFile(imagename, IMAGES);
	}

	/**
	 * Returns a <tt>MapFile</tt> implementation that has the given filename.
	 * 
	 * @param mapname The name of the map file.
	 * @return <tt>MapFile</tt> implementation.
	 */
	public static MapFile getMapFile(String mapname) {

		return (MapFile)getFile(mapname, MAP);
	}

	/**
	 * Returns a <tt>MissionFile</tt> implementation that has the given
	 * filename.
	 * 
	 * @param missionname The name of the mission file.
	 * @return <tt>MissionFile</tt> implementation.
	 */
	public static MissionFile getMissionFile(String missionname) {

		return (MissionFile)getFile(missionname, MISSION);
	}

	/**
	 * Returns a <tt>PaletteFile</tt> implementation that has the given
	 * filename.
	 * 
	 * @param palettename The name of the <tt>PaletteFile</tt>.
	 * @return <tt>PaletteFile</tt> implementation.
	 */
	public static PaletteFile getPaletteFile(String palettename) {

		return (PaletteFile)getFile(palettename, PALETTE);
	}

	/**
	 * Returns a <tt>SoundFile</tt> implementation that has the given
	 * filename.
	 * 
	 * @param soundname The name of the <tt>SoundFile</tt>.
	 * @return <tt>SoundFile</tt> implementation.
	 */
	public static SoundFile getSoundFile(String soundname) {

		return (SoundFile)getFile(soundname, SOUND);
	}

	/**
	 * Returns a <tt>SoundTrackFile</tt> implementation that has the given
	 * filename.
	 * 
	 * @param trackname The name of the <tt>SoundTrackFile</tt>.
	 * @return <tt>SoundtrackFile</tt> implementation.
	 */
	public static SoundTrackFile getSoundTrackFile(String trackname) {

		return (SoundTrackFile)getFile(trackname, SOUNDTRACK);
	}

	/**
	 * Returns the <tt>TextFileBridge</tt> type for any plain-text file type
	 * with the given name.
	 * 
	 * @param textname The name of the text file.
	 * @return <tt>TextFileBridge</tt> class.
	 */
	public static TextFileBridge getTextFileBridge(String textname) {

		String textkey = textname + "(TEXT)";

		// Check for existence in cache
		if (filecache.containsKey(textkey)) {
			return (TextFileBridge)filecache.get(textkey);
		}

		// Check in archives
		ArchiveFile archive = hasFileInArchive(textname);
		if (archive == null) {
			throw new MissingItemException(Strings.getText(FileManagerStringsKeys.MISSING_FILE, textname));
		}

		FilePointer filepointer = archive.getItem(textname);
		TextFileBridge textfile = new TextFileBridge(textname, filepointer);

		// Save to cache
		filecache.put(textkey, textfile);

		return textfile;
	}

	/**
	 * Returns a <tt>VideoFile</tt> implementation that has the given
	 * filename.
	 * 
	 * @param videoname The name of the <tt>VideoFile</tt>.
	 * @return <tt>VideoFile</tt> implementation.
	 */
	public static VideoFile getVideoFile(String videoname) {

		return (VideoFile)getFile(videoname, VIDEO);
	}

	/**
	 * Returns the archive which contains the item.
	 * 
	 * @param name The name of the item being searched for.
	 * @return The archive holding the item, <tt>null</tt> otherwise.
	 */
	private static ArchiveFile hasFileInArchive(String name) {

		for (ArchiveFile archive: archivecache) {
			if (archive.hasItem(name)) {
				return archive;
			}
		}
		return null;
	}

	/**
	 * Initializes this manager by setting-up the caches and loading into them
	 * any file/archive defaults (such as Red Alert / Tiberium Dawn main files).
	 */
	public static void init() {

		// Load files from the resource files directory
		ArrayList<java.io.File> files = listResourceFiles();
		for (java.io.File file: files) {
			String filepath = file.getAbsolutePath();
			FileTypes filetype = FileTypes.getMatchingType(filepath);
			File fileimpl = findImplementationClass(filetype, filepath);

			// Place in appropriate cache
			if (fileimpl instanceof ArchiveFile) {
				loadArchive((ArchiveFile)fileimpl);
			}
			else {
				String filenamekey = file.getAbsolutePath();
				filenamekey = filenamekey.substring(filenamekey.lastIndexOf(java.io.File.separatorChar) + 1);
				loadFile(filenamekey, fileimpl);
			}
		}
	}

	/**
	 * Lists the files in the resource files directory, which are supported by
	 * the game.
	 * 
	 * @return List of filenames for supported files.
	 */
	private static ArrayList<java.io.File> listResourceFiles() {

		java.io.File resourcedir = new java.io.File(RESOURCE_DIR);
		java.io.File[] resourcefiles = resourcedir.listFiles(new FilenameFilter() {
			public boolean accept(java.io.File dir, String name) {
				if (name.contains(".")) {
					return FileTypes.isExtensionSupported(name.substring(name.lastIndexOf('.')));
				}
				return false;
			}
		});
		return new ArrayList<java.io.File>(Arrays.asList(resourcefiles));
	}

	/**
	 * Lists the files of the specified type in the resource files directory.
	 * 
	 * @param type The filetype for the returned list to contain.
	 * @return List of resource files of the given type.
	 */
	private static ArrayList<java.io.File> listResourceFiles(int type) {

		ArrayList<java.io.File> resourcefiles = listResourceFiles();

		// Reduce the list to the specified type only
		for (Iterator<java.io.File> files = resourcefiles.iterator(); files.hasNext(); ) {
			FileTypes filetype = FileTypes.getMatchingType(files.next().getName());
			if ((filetype.type & type) != type) {
				files.remove();
			}
		}
		return resourcefiles;
	}

	/**
	 * Returns a list of image files in the resource directory.
	 * 
	 * @return List of images in the resource directory, which are registered
	 * 		   with the file manager.
	 */
	public static List<java.io.File> listResourceImages() {

		return listResourceFiles(MetaTypes.IMAGE);
	}

	/**
	 * Returns a list of sound files in the resource directory.
	 * 
	 * @return List of sounds in the resource directory, which have been
	 * 		   registered with the file manager.
	 */
	public static List<java.io.File> listResourceSounds() {

		return listResourceFiles(MetaTypes.SOUND);
	}

	/**
	 * Registers a file archive with this manager.
	 * 
	 * @param archive The archive file to load.
	 */
	private static void loadArchive(ArchiveFile archive) {

		// Preload and cache archive
		archive.readFromFile();
		archivecache.add(archive);
	}

	/**
	 * Registers a non-archive file found on the file system with this manager.
	 * 
	 * @param filename The plain name to map the file to.
	 * @param fileimpl The file to register.
	 */
	private static void loadFile(String filename, File fileimpl) {

		// Cache the file
		filecache.put(filename, fileimpl);
	}

	/**
	 * Returns the current resource directory name.  This is either the path to
	 * the currently-loaded mod, or the base resource directory if no mod is
	 * loaded.
	 * 
	 * @return Current resource directory.
	 */
	public static String resourceDirectory() {

		return RESOURCE_DIR;
	}
}
