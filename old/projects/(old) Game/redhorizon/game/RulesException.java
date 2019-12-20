
// ========================================
// Scanner's Java - Missing Rules file data
// ========================================

package redhorizon.game;

/**
 * Exception for any type of problems originating from the rules file, most
 * notably any missing required data.
 * 
 * @author Emanuel Rabina
 */
public class RulesException extends RuntimeException {

	/**
	 * Constructor, takes an error message as parameter.
	 * 
	 * @param message Error message.
	 */
	public RulesException(String message) {

		super(message);
	}
}
