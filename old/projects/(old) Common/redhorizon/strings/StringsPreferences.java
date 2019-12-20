
// ======================================
// Scanner's Java - Strings settings keys
// ======================================

package redhorizon.strings;

import nz.net.ultraq.common.preferences.UserPreferencesKey;

/**
 * Enumeration of settings keys used by the {@link Strings} class.
 * 
 * @author Emanuel Rabina
 */
public enum StringsPreferences implements UserPreferencesKey {

	LOCALE ("Locale", "en");

	private final String key;
	private final String def;

	/**
	 * Constructor, applies the given string as the setting key.
	 * 
	 * @param key The name of the settings parameter.
	 * @param def Default value.
	 */
	private StringsPreferences(String key, String def) {

		this.key = key;
		this.def = def;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefault() {

		return def;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getKey() {

		return key;
	}
}
