
// ===================================
// Scanner's Java - Main menu routines
// ===================================

package redhorizon.launcher;

/**
 * Abstract class which contains basic functionality for what should happen in
 * the main menu GUI, as well as definitions for other routines that
 * implementing classes should fill-in.
 * 
 * @author Emanuel Rabina
 */
public abstract class MainMenu {

	protected boolean exitcode;

	/**
	 * Default constructor.
	 */
	protected MainMenu() {
	}

	/**
	 * Creates a new main menu.
	 * 
	 * @return Main menu.
	 */
	public static MainMenu createMainMenu() {

		return new MainMenuSWT();
	}

	/**
	 * Returns the exit code, used to signify whether or not to break-out of the
	 * gameplay loop.
	 * 
	 * @return <code>true</code> if the gameplay loop should be broken,
	 * 		   <code>false</code> otherwise.
	 */
	public boolean exitCode() {

		return exitcode;
	}
}
