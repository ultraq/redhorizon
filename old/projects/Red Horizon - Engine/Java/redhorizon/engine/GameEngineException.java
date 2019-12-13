
// =========================================
// Scanner's Java - Engine running exception
// =========================================

package redhorizon.engine;

/**
 * Exception for issues that arise during game engine execution, particularly in
 * the several threads that comprise the game engine.
 * 
 * @author Emanuel Rabina
 */
public class GameEngineException extends RuntimeException {

	/**
	 * Constructor, takes the original exception and an error message as
	 * parameters.
	 * 
	 * @param message Additional error message.
	 * @param cause	  The original exception.
	 */
	public GameEngineException(String message, Throwable cause) {

		super(message, cause);
	}
}
