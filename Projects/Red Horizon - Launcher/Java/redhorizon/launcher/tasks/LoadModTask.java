
package redhorizon.launcher.tasks;

import redhorizon.filetypes.ini.IniFile;
import redhorizon.launcher.mod.Mod;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Common code for the loading of a mod in the Red Horzion Mods directory.
 * 
 * @author Emanuel Rabina
 */
public abstract class LoadModTask implements SplashScreenTask {

	protected static final String MOD_DIRECTORY = "Mods";

	private final String moddir;
	private final String modname = "";
//	private final String modversion;

	/**
	 * Constructor, set the name/directory of the mod.
	 * 
	 * @param moddir Name of the directory the mod resides in, within the Mods
	 * 				 directory.
	 */
	protected LoadModTask(String moddir) {

		this.moddir = MOD_DIRECTORY + "/" + moddir;

		// Attempt to obtain mod information out of the given directory
		FileSystem filesystem = FileSystems.getDefault();
		Path modpath = filesystem.getPath(this.moddir);
		Mod mod = new Mod(modpath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doTask() {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String taskText() {

		return "Loading mod '" + modname + "'";
	}
}
