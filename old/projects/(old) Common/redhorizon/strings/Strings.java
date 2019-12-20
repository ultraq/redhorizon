
// ================================================
// Scanner's Java - Locale-specific program strings
// ================================================

package redhorizon.strings;

import nz.net.ultraq.common.preferences.Preferences;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class to access all locale-specific program strings (such as text for the
 * interface, text for in-game, error messages, etc).  The different text for
 * each localization can be found under the localizations source directory.
 * 
 * @author Emanuel Rabina
 */
public class Strings {

	private static Locale locale;
	private static HashMap<String,ResourceBundle> messagebundles = new HashMap<String,ResourceBundle>();
	private static NumberFormat percentformatter;

	static {
		String localestring = Preferences.get(StringsPreferences.LOCALE);
		if (localestring.contains("_")) {
			String language = localestring.substring(0, localestring.indexOf('_'));
			String country  = localestring.substring(localestring.indexOf('_') + 1);
			locale = new Locale(language, country);
		}
		else {
			locale = new Locale(localestring);
		}

		// Create appropriate number formatters
		percentformatter = NumberFormat.getPercentInstance(locale);
	}

	/**
	 * Hidden default constructor as this class is only ever meant to be used
	 * statically.
	 */
	private Strings() {
	}

	/**
	 * Converts the floating-point value into a percentage string.
	 *
	 * @param floatval The floating-point number.
	 * @return The value as a percentage, in the user's selected locale.
	 */
	public static String floatToPercent(float floatval) {

		return percentformatter.format(floatval);
	}

	/**
	 * Returns localized text from the <code>ResourceBundle</code> which was
	 * added with the given enumerated type.
	 *
	 * @param stringskey Object containing the resource bundle and parameter
	 * 					 name of the string to retrieve.
	 * @param strings	 List of replacement strings for the {n} values in the
	 * 					 message text.
	 * @return Localized text for the given key.
	 */
	public static String getText(StringsKey stringskey, String... strings) {

		return getText(stringskey.getKey(), stringskey.getResourceBundle(), strings);
	}

	/**
	 * Returns the piece of localized text associated with the key.
	 *
	 * @param textkey	 Name of the parameter in the resource bundle.
	 * @param textbundle Name of the resource bundle containing the key.
	 * @return Localized text for the given key.
	 */
	private static String getText(String textkey, String textbundle) {

		if (!messagebundles.containsKey(textbundle)) {
			messagebundles.put(textbundle, ResourceBundle.getBundle(textbundle, locale));
		}
		return messagebundles.get(textbundle).getString(textkey);
	}

	/**
	 * Returns the piece of localized text associated with the key, using the
	 * replacement text provided in the <code>String</code> array.
	 *
	 * @param textkey	 Name of the parameter in the resource bundle.
	 * @param textbundle Name of the resource bundle containing the key.
	 * @param strings Array of replacement strings for the {n} values in the
	 * 				   properties file.
	 * @return Localized text for the given key.
	 */
	private static String getText(String textkey, String textbundle, String... strings) {

		// NOTE: Standard property substitution in Java doesn't work the way I
		//		 want.  The below fixes that.
		StringBuilder text = new StringBuilder(getText(textkey, textbundle));
		for (int i = 0; i < strings.length; i++) {
			while (true) {
				int nextparam = text.indexOf("{" + i + "}");
				if (nextparam == -1) {
					break;
				}
				text.replace(nextparam, nextparam + 3, strings[i]);
			}
		}
		return text.toString();
	}

	/**
	 * Converts the percentage-string into a floating-point value.
	 *
	 * @param percentval Locale-specific percentage value.
	 * @return The percentage as a floating-point value.
	 * @throws StringsException If <tt>percentval</tt> could not be parsed and
	 * 		   converted into a <tt>float</tt> type.
	 */
	public static float percentToFloat(String percentval) throws StringsException {

		try {
			return percentformatter.parse(percentval).floatValue();
		}
		catch (ParseException ex) {
			throw new StringsException(ex.getMessage(), ex);
		}
	}
}
