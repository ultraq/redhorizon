
// ===================================
// Scanner's Java - Rules strings keys
// ===================================

package redhorizon.game;

import redhorizon.strings.StringsKey;

/**
 * Enumeration of keys to strings for Rules-related messages.
 * 
 * @author Emanuel Rabina
 */
public enum RulesStringsKeys implements StringsKey {

	// Configuration errors
	MISSING_DATA,
	UNKNOWN_FACTION,
	UNKNOWN_UNIT,
	UNKNOWN_ATTRIBUTE_UNIT;

	private static final String RESOURCEBUNDLE_RULES = "Game_Rules";

	/**
	 * @inheritDoc
	 */
	public String getKey() {

		return name();
	}

	/**
	 * @inheritDoc
	 */
	public String getResourceBundle() {

		return RESOURCEBUNDLE_RULES;
	}
}
