
// ======================================
// Scanner's Java - Options menu routines
// ======================================

package redhorizon.launcher;

import redhorizon.engine.EngineSettingsKeys;
import redhorizon.engine.display.DisplaySettingsKeys;
import redhorizon.engine.display.ScreenResolutions;
import redhorizon.settings.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class definition for the Options Menu GUI.  Provides the states and
 * access to each setting for the GUI implementation to display in their way.
 * 
 * @author Emanuel Rabina
 */
public abstract class OptionsMenu {

	// Game options
	int scrollxy;
	int scrollz;

	static final int SCROLLXY_MIN = 1;
	static final int SCROLLXY_MAX = 120;
	static final int SCROLLZ_MIN  = 0;
	static final int SCROLLZ_MAX  = 2;

	// Video options
	boolean fullscreen;
	String fullscreenres;
	String windowedres;
	float scanlines;
	List<String> resolutions;

	static final int LINE_OPACITY_MIN = 0;
	static final int LINE_OPACITY_MAX = 100;

	// Audio options
	float mainvolume;
	float hudvolume;
	float musvolume;
	float sfxvolume;
	float vidvolume;

	static final int VOLUME_MIN = 0;
	static final int VOLUME_MAX = 100;

	// Development options
	boolean debug;

	/**
	 * Constructor, generates the available settings and their states.
	 */
	OptionsMenu() {

		// Set variables
		restoreSettings();

		// Set constants
		resolutions = new ArrayList<String>();
		for (ScreenResolutions res: ScreenResolutions.values()) {
			resolutions.add(res.toString());
		}
	}

	/**
	 * Applies the current state of the settings to the
	 * <code>RedHorizon.ini</code> settings file.  GUI implementations should
	 * call this method with either the selection of an Apply, OK, or Save
	 * command.
	 */
	void applySettings() {

		// Save game options
		Settings.setSetting(EngineSettingsKeys.SCROLLSPEEDXY, Integer.toString(scrollxy));
//		Settings.setSetting(GameSettings.SCROLLSPEEDZ, Integer.toString(scrollz));

		// Save video options
		Settings.setSetting(DisplaySettingsKeys.FULLSCREEN, Boolean.toString(fullscreen));
		Settings.setSetting(DisplaySettingsKeys.FULLSCREENRES, fullscreenres);
		Settings.setSetting(DisplaySettingsKeys.WINDOWEDRES, windowedres);
//		Settings.setSetting(DisplaySettingsKeys.SCANLINES, Float.toString(scanlines));

		// Save audio options
		Settings.setSetting(EngineSettingsKeys.VOLUMEMASTER, Float.toString(mainvolume));
//		Settings.setSetting(GameSettings.VOLUMEHUD, Float.toString(hudvolume));
//		Settings.setSetting(GameSettings.VOLUMEMUSIC, Float.toString(musvolume));
//		Settings.setSetting(GameSettings.VOLUMESOUND, Float.toString(sfxvolume));
//		Settings.setSetting(GameSettings.VOLUMEVIDEO, Float.toString(vidvolume));

		// Save development settings
		System.setProperty("debug", Boolean.toString(debug));
	}

	/**
	 * Takes the settings in their current state from the <code>Settings</code>
	 * class, and re-applies them.
	 */
	void restoreSettings() {

		// Set game options
		scrollxy = Integer.parseInt(Settings.getSetting(EngineSettingsKeys.SCROLLSPEEDXY));

		// Set video options
		fullscreen    = Boolean.parseBoolean(Settings.getSetting(DisplaySettingsKeys.FULLSCREEN));
		fullscreenres = Settings.getSetting(DisplaySettingsKeys.FULLSCREENRES);
		windowedres   = Settings.getSetting(DisplaySettingsKeys.WINDOWEDRES);
//		scanlines     = Float.parseFloat(Settings.getSetting(GameSettings.SCANLINES));

		// Set audio options
		mainvolume = Float.parseFloat(Settings.getSetting(EngineSettingsKeys.VOLUMEMASTER));
//		hudvolume  = Float.parseFloat(Settings.getSetting(GameSettings.VOLUMEHUD));
//		musvolume  = Float.parseFloat(Settings.getSetting(GameSettings.VOLUMEMUSIC));
//		sfxvolume  = Float.parseFloat(Settings.getSetting(GameSettings.VOLUMESOUND));
//		vidvolume  = Float.parseFloat(Settings.getSetting(GameSettings.VOLUMEVIDEO));

		// Set development options
		debug = Boolean.getBoolean("debug");
	}
}
