
package redhorizon.launcher.tasks;

import redhorizon.launcher.mod.Mod;
import redhorizon.resourcemanager.ResourceManager;
import redhorizon.resourcemanager.scanner.DirectoryScanner;

import java.nio.file.Path;

/**
 * Common code for the loading of a mod in the Red Horzion Mods directory.
 * 
 * @author Emanuel Rabina
 */
public abstract class LoadModTask implements SplashScreenTask {

	private static final String MIX_FILE_RESOURCE_LOCATOR = "MixFileResourceLocator";

	protected static final String MOD_DIRECTORY = "Mods";

	private final Path modpath;
	private final String modname;

	/**
	 * Constructor, set the name/directory of the mod.
	 * 
	 * @param modpath Path to the location of the mod to have loaded.
	 */
	protected LoadModTask(Path modpath) {

		this.modpath = modpath;

		// Attempt to obtain mod information out of the given directory
		Mod mod = new Mod(modpath);
		modname = mod.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doTask() {

		MixFileScannerListener mixfilescannerlistener = new MixFileScannerListener();
		new DirectoryScanner(modpath.toString(), mixfilescannerlistener).scan();
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
