
package redhorizon.filetypes;

/**
 * Exception to be thrown when the reading of a file runs into some file
 * information, normally in the header, that it cannot interpret.
 * 
 * @author Emanuel Rabina
 */
public class UnsupportedFileException extends RuntimeException {

	/**
	 * Constructor, takes an error message as parameter.
	 * 
	 * @param message Additional error message.
	 */
	public UnsupportedFileException(String message) {

		super(message);
	}
}
