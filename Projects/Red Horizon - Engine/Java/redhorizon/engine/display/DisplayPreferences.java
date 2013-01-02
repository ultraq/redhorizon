
// ======================================
// Scanner's Java - Display settings keys
// ======================================

package redhorizon.engine.display;

import nz.net.ultraq.common.preferences.UserPreferencesKey;

/**
 * Enumeration of settings keys used by the display package.
 * 
 * @author Emanuel Rabina
 */
public enum DisplayPreferences implements UserPreferencesKey {

	// Video options
	DISPLAY_MODE   ("DisplayMode", "window"),
	DISPLAY_WIDTH  ("Width",  "800"),
	DISPLAY_HEIGHT ("Height", "600");

	private final String key;
	private final String def;

	/**
	 * Constructor, applies the given string as the setting key.
	 * 
	 * @param key The name of the settings parameter.
	 * @param def Default value for this setting.
	 */
	private DisplayPreferences(String key, String def) {

		this.key = key;
		this.def = def;
	}

	/**
	 * @inheritDoc
	 */
	public String getKey() {

		return key;
	}

	/**
	 * @inheritDoc
	 */
	public String getDefault() {

		return def;
	}
}
