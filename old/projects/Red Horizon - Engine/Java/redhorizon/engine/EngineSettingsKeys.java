
// =====================================
// Scanner's Java - Engine setitngs keys
// =====================================

package redhorizon.engine;

import nz.net.ultraq.common.preferences.UserPreferencesKey;

/**
 * Enumeration of keys to Red Horizon engine settings.
 * 
 * @author Emanuel Rabina
 */
public enum EngineSettingsKeys implements UserPreferencesKey {

	// Control options
	SCROLLSPEEDXY ("ScrollSpeedXY", "1.0"),

	// Audio options
	VOLUMEMASTER  ("VolumeMaster", "1.0");

	private final String key;
	private final String def;

	/**
	 * Constructor, applies the given string as the setting key.
	 * 
	 * @param key The name of the settings parameter.
	 * @param def The default value.
	 */
	private EngineSettingsKeys(String key, String def) {

		this.key = key;
		this.def = def;
	}

	/**
	 * @inheritDoc
	 */
	public String getDefault() {

		return def;
	}

	/**
	 * @inheritDoc
	 */
	public String getKey() {

		return key;
	}

}
