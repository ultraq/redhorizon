
// ==========================================
// Scanner's Java - Bad setting in rules file
// ==========================================

package redhorizon.game;

/**
 * Exception used to indicate the discovery of a setting in the Rules file which
 * is unrecognized for the implementation of the Rules file.
 * 
 * @author Emanuel Rabina
 */
public class UnknownAttributeException extends RuntimeException {

	/**
	 * Constructor, takes an error message as parameter.
	 * 
	 * @param message Error message.
	 */
	public UnknownAttributeException(String message) {

		super(message);
	}
}
