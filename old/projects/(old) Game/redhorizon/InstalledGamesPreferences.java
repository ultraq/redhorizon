
// ===================================
// Scanner's Java - Game settings keys
// ===================================

package redhorizon;

import nz.net.ultraq.common.preferences.SystemPreferencesKey;

/**
 * Enumeration of settings relevant to the entire game.
 * 
 * @author Emanuel Rabina
 */
public enum InstalledGamesPreferences implements SystemPreferencesKey {

	// Installation settings
	RED_ALERT     ("RAInstalled", "false"),
	TIBERIUM_DAWN ("TDInstalled", "false");

	private final String key;
	private final String def;

	/**
	 * Constructor, applies the given string as the setting key.
	 * 
	 * @param key The name of the preference.
	 * @param def Default value for the preference.
	 */
	private InstalledGamesPreferences(String key, String def) {

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
