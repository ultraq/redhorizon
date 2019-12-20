
// ===========================================
// Scanner's Java - List of available tilesets
// ===========================================

package redhorizon.game.map;

/**
 * Contains a list of the available theaters used in Red Horizon.
 * 
 * @author Emanuel Rabina
 */
enum Theaters {

	// Available theater types
	DESERT    ("desert",   ".des"),
	INTERIOR  ("interior", ".int"),
	SNOW      ("snow",     ".sno"),
	TEMPERATE ("temperat", ".tem"),
	WINTER    ("winter",   ".win");

	public final String label;
	public final String ext;

	/**
	 * Constructor, initializes each enumerated type.
	 * 
	 * @param label The name used in scenario files to denote this theater.
	 * @param ext	File extension for the template files of this theater type.
	 */
	private Theaters(String label, String ext) {

		this.label = label;
		this.ext   = ext;
	}

	/**
	 * Attempts to locate the matching enumerated type given the match
	 * parameter - the label as used in the scenario files.
	 * 
	 * @param match The <code>String</code> representation of the type's label
	 * 				to match-up.
	 * @return The matching enumerated type.
	 */
	public static Theaters getMatchingType(String match) {

		for (Theaters types: Theaters.values()) {
			if (types.label.equalsIgnoreCase(match)) {
				return types;
			}
		}
		throw new EnumConstantNotPresentException(Theaters.class, match);
	}
}
