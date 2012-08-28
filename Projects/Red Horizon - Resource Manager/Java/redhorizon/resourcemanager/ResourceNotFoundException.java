
package redhorizon.resourcemanager;

/**
 * Exception thrown when a resource cannot be located.
 * 
 * @author Emanuel Rabina
 */
public class ResourceNotFoundException extends RuntimeException {

	/**
	 * Constructor, sets a message.
	 * 
	 * @param msg
	 */
	public ResourceNotFoundException(String msg) {

		super(msg);
	}

	/**
	 * Constructor, sets a message and cause.
	 * 
	 * @param msg
	 * @param cause
	 */
	public ResourceNotFoundException(String msg, Throwable cause) {

		super(msg, cause);
	}
}
