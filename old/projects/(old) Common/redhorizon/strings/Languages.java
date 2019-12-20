
// ============================================
// Scanner's Java - List of supported languages
// ============================================

package redhorizon.strings;

import java.util.Locale;

/**
 * Contains the list of currently supported languages.  Is basically a mapping
 * of the text used to describe the selected language, into the strings used to
 * form a <code>Locale</code> object.
 * 
 * @author Emanuel Rabina
 */
public enum Languages {

	ENGLISH (Locale.ENGLISH, "English");

	public final Locale locale;
	public final String label;

	/**
	 * Constructor, sets the adds how the language should be displayed.
	 * 
	 * @param locale <code>Locale</code> object representing the
	 * 				 language/locale.
	 * @param label	 How the name of the language should be shown.
	 */
	private Languages(Locale locale, String label) {

		this.locale = locale;
		this.label  = label;
	}

	/**
	 * Attempts to locate the matching locale given the supported locale's
	 * string representation from the settings file.
	 * 
	 * @param match The <code>String</code> representation of the type's locale
	 * 				to match-up.
	 * @return The matching enumerated type.
	 */
	public static Locale getMatchingLocale(String match) {

		for (Languages locale: Languages.values()) {
			if (locale.label.equals(match)) {
				return locale.locale;
			}
		}
		throw new EnumConstantNotPresentException(Languages.class, match);
	}
}
