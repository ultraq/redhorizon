
// ========================================================
// Scanner's Java - Exception for during the gameplay phase
// ========================================================

package redhorizon.engine;

/**
 * Wrapper for all exceptions that occur during the playing of the game.
 * 
 * @author Emanuel Rabina
 */
public class GameFlowException extends RuntimeException {

	/**
	 * Constructor, takes the original exception and an error message as
	 * parameters.
	 * 
	 * @param message	Additional error message.
	 * @param exception	The original exception.
	 */
	public GameFlowException(String message, Exception exception) {

		super(message, exception);
	}
}
