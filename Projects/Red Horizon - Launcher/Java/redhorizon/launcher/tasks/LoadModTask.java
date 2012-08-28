
package redhorizon.launcher.tasks;

import redhorizon.launcher.mod.Mod;
import redhorizon.resourcemanager.ResourceManager;
import redhorizon.utilities.scanner.DirectoryScanner;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Common code for the loading of a mod in the Red Horzion Mods directory.
 * 
 * @author Emanuel Rabina
 */
public abstract class LoadModTask implements SplashScreenTask {

	private static final String MIX_FILE_RESOURCE_LOCATOR = "MixFileResourceLocator";

	protected static final String MOD_DIRECTORY = "Mods";

	private final String moddir;
	private final String modname;

	/**
	 * Constructor, set the name/directory of the mod.
	 * 
	 * @param moddir Name of the directory the mod resides in, within the Mods
	 * 				 directory.
	 */
	protected LoadModTask(String moddir) {

		this.moddir = MOD_DIRECTORY + "/" + moddir;

		// Attempt to obtain mod information out of the given directory
		Path modpath = Paths.get(this.moddir);
		Mod mod = new Mod(modpath);
		modname = mod.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doTask() {

		MixFileScannerListener mixfilescannerlistener = new MixFileScannerListener();
		new DirectoryScanner(moddir, mixfilescannerlistener).scan();
		ResourceManager.addResourceLocator(MIX_FILE_RESOURCE_LOCATOR, mixfilescannerlistener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String taskText() {

		return "Loading mod '" + modname + "'";
	}
}
