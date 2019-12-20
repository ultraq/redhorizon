
// ============================================
// Scanner's Java - Exception in Strings
// ============================================

package redhorizon.strings;

/**
 * Generic exception for any exceptions that might occur from incorrect use of
 * the {@link Strings} class.
 *
 * @author Emanuel Rabina
 */
public class StringsException extends RuntimeException {

	/**
	 * Constructor, takes an error message as parameter.
	 *
	 * @param message Additional error message.
	 */
	public StringsException(String message) {

		super(message);
	}

	/**
	 * Constructor, takes the original exception and an error message as
	 * parameters.
	 *
	 * @param message Additional error message.
	 * @param cause	  The original exception.
	 */
	public StringsException(String message, Throwable cause) {

		super(message, cause);
	}
}
