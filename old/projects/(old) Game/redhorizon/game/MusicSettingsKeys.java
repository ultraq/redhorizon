
// ======================================
// Scanner's Java - Jukebox settings keys
// ======================================

package redhorizon.game;

import redhorizon.settings.SettingsKey;

/**
 * Enumeration of keys to settings for the jukebox / music player of Red
 * Horizon.
 * 
 * @author Emanuel Rabina
 */
public enum MusicSettingsKeys implements SettingsKey {

	// Jukebox settings
	MUSICREPEAT   ("MusicRepeat"),
	MUSICSHUFFLE  ("MusicShuffle");

	private static String SECTION_GAME = "Game";

	private final String key;

	/**
	 * Constructor, applies the given string as the setting key.
	 * 
	 * @param key The name of the settings parameter.
	 */
	private MusicSettingsKeys(String key) {

		this.key = key;
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
	public String getSection() {

		return SECTION_GAME;
	}
}
