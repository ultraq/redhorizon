
// ============================================================
// Scanner's Java - Exceptions to the extension implementations
// ============================================================

package redhorizon.filemanager;


/**
 * Normally, the implementation class to load for a given file can be derived
 * from the file extension.  This isn't always the case however.  So for such
 * exceptions, I've created this list of names which should override the default
 * implementation as defined in {@link FileTypes}.
 * 
 *  @author Emanuel Rabina
 */
enum FileTypesOverride {

	// Map parts
	OVERLAY_ORE   ("GOLD0[1-4]\\.(int|sno|tem)",  "shp.ShpFile"),
	OVERLAY_GEM   ("GEM0[1-4]\\.(int|sno|tem)",   "shp.ShpFile"),

	TERRAIN_TREES ("TC?\\d{1,2}[.](int|sno|tem)", "shp.ShpFile"),

	// Civilian structures
	STRUCTURE_CIVILIAN ("V\\d{2}\\.(int|sno|tem)", "shp.ShpFile");

	final String pattern;
	final String implementation;

	/**
	 * Constructor, initializes each enumerated type.
	 * 
	 * @param pattern		 The regular expression to match with file names.
	 * @param implementation The implementation class for any file that matches
	 * 						 the pattern.
	 */
	private FileTypesOverride(String pattern, String implementation) {

		this.pattern        = pattern;
		this.implementation = "redhorizon.filetypes." + implementation;
	}

	/**
	 * Returns the overriding implementation name for the given match parameter.
	 * 
	 * @param match The filename of the file to look up.
	 * @return The path to the implementing class to use.
	 */
	static String getImplementation(String match) {

		for (FileTypesOverride override: FileTypesOverride.values()) {
			if (match.matches(override.pattern)) {
				return override.implementation;
			}
		}
		throw new EnumConstantNotPresentException(FileTypesOverride.class, match);
	}

	/**
	 * Returns whether or not the given <code>String</code> matches any of the
	 * patterns in this enum.
	 * 
	 * @param match The String to test against.
	 * @return <code>true</code> if there is a match with one of the patterns,
	 * 		   <code>false</code> otherwise.
	 */
	static boolean hasMatchingType(String match) {

		for (FileTypesOverride override: FileTypesOverride.values()) {
			if (match.matches(override.pattern)) {
				return true;
			}
		}
		return false;
	}
}
