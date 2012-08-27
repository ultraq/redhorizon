
package redhorizon.launcher.mod;

import redhorizon.filetypes.ini.IniFile;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * Contains information about a mod, gleamed from the mod.ini file found in a
 * mod directory.
 * 
 * @author Emanuel Rabina
 */
public class Mod {

	private static final String MOD_INI_FILE1 = "Mod.ini";
//	private static final String MOD_INI_FILE2 = "mod.ini";

	/**
	 * Constructor, attempt to read all mod information from a given path, first
	 * using the Mod.ini file (if any), then by scanning the path for other mod
	 * files.
	 * 
	 * @param path Path to the directory or file containing the mod.
	 */
	public Mod(Path path) {

		// Find Mod.ini file
		FileSystem filesystem = path.getFileSystem();
		Path modinipath = filesystem.getPath(path.toString(), MOD_INI_FILE1);
		IniFile modinifile = new IniFile(MOD_INI_FILE1, FileChannel.open(modinipath));
	}
}
