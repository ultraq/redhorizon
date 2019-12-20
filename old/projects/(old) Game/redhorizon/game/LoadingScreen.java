
// ===============================
// Scanner's Java - Loading screen
// ===============================

package redhorizon.game;

import redhorizon.engine.scenegraph.SceneManager;
import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.ImageFile;
import redhorizon.launcher.LauncherSettingsKeys;
import redhorizon.media.ImageScaled;
import redhorizon.misc.geometry.Point3D;
import redhorizon.settings.Settings;
import static redhorizon.game.GameObjectDepths.DEPTH_MEDIA_LOADINGSCREEN;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class which can create a loading screen for use in transitions of loading
 * game content or just intermissions in general.  The screen created will be a
 * random image from a list of appropriate images within the installed games,
 * plus image files in the custom directory prefixed by the string
 * "Loading_".<br>
 * <br>
 * TODO: In future, text should also be involved to increase the immersive
 * 		 experience of the EVA as it was in the original C&Cs.
 * 
 * @author Emanuel Rabina.
 */
public class LoadingScreen extends ImageScaled {

	// Locations of loading screen images, and the current loading screen
	private static final String[] imagesTD = { };
	private static final String[] imagesRA = {
		"aftr_hi", "aly1", "apc_hi", "aphi0049", "bnhi0020", "dchi0040",
		"frhi0166", "lab", "landsbrg", "mahi0107", "mig_hi", "mtfacthi",
		"needle", "sov2", "spy", "stalin", "tent"
	};

	private static ArrayList<String> images;
	private static LoadingScreen loadingscreen;

	/**
	 * Constructor, creates a loading screen using the given image filename.
	 * 
	 * @param imagefile File of the image to use.
	 */
	private LoadingScreen(ImageFile imagefile) {

		super(imagefile);
	}

	/**
	 * Constructs the list of available images to use for the loading screen.
	 */
	private static void createImagesList() {

		// Locate custom image files, convert to filenames
		images = new ArrayList<String>();
		for (File file: FileManager.listResourceImages()) {
			images.add(file.getName());
		}

		// Reduce returned list to just "Loading_" images
		ArrayList<String> removals = new ArrayList<String>();
		for (String imagename: images) {
			if (!imagename.startsWith("Loading_")) {
				removals.add(imagename);
			}
		}
		images.removeAll(removals);

		// Append known game-specific images
		if (Boolean.parseBoolean(Settings.getSetting(InstalledGamesPreferences.INSTALLED_REDALERT))) {
			Collections.addAll(images, imagesRA);
		}
		if (Boolean.parseBoolean(Settings.getSetting(InstalledGamesPreferences.INSTALLED_TIBERIUMDAWN))) {
			Collections.addAll(images, imagesTD);
		}
	}

	/**
	 * Creates a new loading screen using a random image from those available in
	 * the game's directory structure.
	 * 
	 * @return The new loading screen.
	 */
	public static LoadingScreen createLoadingScreen() {

		// Construct the list of loading images
		if (images == null) {
			createImagesList();
		}
		// Select a random index within the image list
		int screenpick = (int)(Math.random() * images.size());
		String imagename = images.get(screenpick);

		// Build the loading screen
		loadingscreen = new LoadingScreen(FileManager.getImageFile(imagename));
		SceneManager.currentSceneManager().getRootSceneNode().attachChildObject(loadingscreen);
		loadingscreen.setPosition(new Point3D(0, 0, DEPTH_MEDIA_LOADINGSCREEN));
		return loadingscreen;
	}

	/**
	 * Deletes the current loading screen by removing it from the graphics
	 * engine.
	 */
	public static void deleteLoadingScreen() {

		SceneManager.currentSceneManager().getRootSceneNode().detachChildObject(loadingscreen.node);
		loadingscreen = null;
	}
}
