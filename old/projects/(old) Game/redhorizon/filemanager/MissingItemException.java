
// =========================================================
// Scanner's Java - Exception for content that doesn't exist
// =========================================================

package redhorizon.filemanager;

/**
 * An exception for content that may try to be accessed by the
 * {@link redhorizon.filemanager.FileManager} but either hasn't yet been
 * registered with the content factories or just doesn't exist.
 * 
 * @author Emanuel Rabina
 */
public class MissingItemException extends RuntimeException {

	/**
	 * Constructor, takes an error message as parameter.
	 * 
	 * @param message Additional error message.
	 */
	public MissingItemException(String message) {

		super(message);
	}
}
