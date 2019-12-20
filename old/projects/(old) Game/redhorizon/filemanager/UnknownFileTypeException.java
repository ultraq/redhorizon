
// ===================================================
// Scanner's Java - Exception for unknown file formats
// ===================================================

package redhorizon.filemanager;

/**
 * Exception to be thrown when the game attempts to load an unknown file
 * type/format into a known one, or a known file type that isn't yet supported.
 * 
 * @author Emanuel Rabina
 */
public class UnknownFileTypeException extends RuntimeException {

	/**
	 * Constructor, takes an error message as parameter.
	 * 
	 * @param message Additional error message.
	 */
	public UnknownFileTypeException(String message) {

		super(message);
	}
}
