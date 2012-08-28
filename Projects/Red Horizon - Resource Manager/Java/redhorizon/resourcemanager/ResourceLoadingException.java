
package redhorizon.resourcemanager;

/**
 * Exception thrown when a resource cannot be loaded.
 * 
 * @author Emanuel Rabina
 */
public class ResourceLoadingException extends RuntimeException {

	/**
	 * Constructor, set the cause of the loading exception.
	 * 
	 * @param cause
	 */
	ResourceLoadingException(Throwable cause) {

		super(cause);
	}
}
