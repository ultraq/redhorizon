
// ================================================
// Scanner's Java - Exception for no TD or RA discs
// ================================================

package redhorizon.filemanager;

/**
 * Exception for when the user opts to locate a Red Alert or Tiberium Dawn discs
 * (at least one of these games is required to play Red Horizon).
 * 
 * @author Emanuel Rabina
 */
public class MissingFileException extends RuntimeException {

	/**
	 * Constructor, takes an error message as parameter.
	 * 
	 * @param message Additional error message.
	 */
	public MissingFileException(String message) {

		super(message);
	}
}
