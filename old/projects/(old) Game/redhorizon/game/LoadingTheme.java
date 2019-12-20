
// ==============================
// Scanner's Java - Loading music
// ==============================

package redhorizon.game;

import redhorizon.engine.scenegraph.SceneManager;
import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.SoundTrackFile;
import redhorizon.launcher.LauncherSettingsKeys;
import redhorizon.media.SoundTrack;
import redhorizon.settings.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class which can create a loading theme music for use in transitions of
 * loading game content or just intermissions in general.  The music created
 * will be a random track from a list of known ambience tracks, plus those found
 * in the custom files directory prefixed by the string "Loading_".
 * 
 * @author Emanuel Rabina.
 */
public class LoadingTheme extends SoundTrack {

	// Locations of loading themes, and the current loading theme
	private static final String[] themesTD = { };
	private static final String[] themesRA = { "intro", "map" };
	private static ArrayList<String> themes;
	private static LoadingTheme loadingtheme;

	/**
	 * Constructor, creates a loading theme music track using the given sound
	 * filename.
	 * 
	 * @param trackfile File of the sound to use.
	 */
	private LoadingTheme(SoundTrackFile trackfile) {

		super(trackfile);
	}

	/**
	 * Constructs the list of available themes to use for the loading theme.
	 */
	private static void createThemesList() {

		// Locate custom themes, convert to filenames
		themes = new ArrayList<String>();
		for (File file: FileManager.listResourceSounds()) {
			themes.add(file.getName());
		}

		// Locate custom image files
		ArrayList<String> removals = new ArrayList<String>();

		// Reduce returned list to just "Loading_" images
		for (String themename: themes) {
			if (!themename.startsWith("Loading_")) {
				removals.add(themename);
			}
		}
		themes.removeAll(removals);

		// Append known game-specific themes
		if (Boolean.parseBoolean(Settings.getSetting(InstalledGamesPreferences.INSTALLED_REDALERT))) {
			Collections.addAll(themes, themesRA);
		}
		if (Boolean.parseBoolean(Settings.getSetting(InstalledGamesPreferences.INSTALLED_TIBERIUMDAWN))) {
			Collections.addAll(themes, themesTD);
		}
	}

	/**
	 * Creates a new loading theme using a random sound/track from those
	 * available in the game's directory structure.
	 * 
	 * @return The new loading theme.
	 */
	public static LoadingTheme createLoadingTheme() {

		// Construct the list of loading themes
		if (themes == null) {
			createThemesList();
		}
		// Select a random index within the theme list
		int screenpick = (int)(Math.random() * themes.size());
		String trackname = themes.get(screenpick);

		// Build the loading theme
		loadingtheme = new LoadingTheme(FileManager.getSoundTrackFile(trackname));
		SceneManager.currentSceneManager().getRootSceneNode().attachChildObject(loadingtheme);
		return loadingtheme;
	}

	/**
	 * Deletes the current loading music by removing it from the audio engine.
	 */
	public static void deleteLoadingTheme() {

		SceneManager.currentSceneManager().getRootSceneNode().detachChildObject(loadingtheme.node);
		loadingtheme = null;
	}
}
