
// ======================================
// Scanner's Java - Sub-engine exceptions
// ======================================

package redhorizon.engine;

/**
 * Exception which can be thrown by any stage in the sub-engine lifecycle.
 * 
 * @author Emanuel Rabina
 */
public class EngineException extends RuntimeException {

	/**
	 * Constructor, takes an error message as a parameter.
	 * 
	 * @param message Error message.
	 */
	public EngineException(String message) {

		super(message);
	}

	/**
	 * Constructor, takes the original exception and an error message as
	 * parameters.
	 * 
	 * @param message Additional error message.
	 * @param cause	  The original exception.
	 */
	public EngineException(String message, Throwable cause) {

		super(message, cause);
	}
}
