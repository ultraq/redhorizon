
package redhorizon.utilities.converter;

/**
 * Exception thrown by the converter classes to indicate that the conversion
 * couldn't be performed because it isn't supported.
 * 
 * @author Emanuel Rabina
 */
public class UnsupportedConversionException extends RuntimeException {

	/**
	 * Constructor, set a message and the cause of the conversion not being
	 * supported.
	 * 
	 * @param message
	 * @param cause
	 */
	public UnsupportedConversionException(String message, Throwable cause) {

		super(message, cause);
	}
}
