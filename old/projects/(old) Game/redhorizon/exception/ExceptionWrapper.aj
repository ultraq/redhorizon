
// ================================================
// Scanner's AspectJ - Red Horizon exception phases
// ================================================

package redhorizon.exception;

import redhorizon.Main;
import redhorizon.GameplayException;
import redhorizon.ShutdownException;
import redhorizon.StartupException;

/**
 * As a part of the exception mechanism of Red Horizon, an exception should be
 * pinpointed to the phase of the game in which it belongs.  This aspect wraps
 * around those phases of the game, catching softened exceptions and then
 * putting them inside a phase-specific exception, so that the error message
 * created at the end indicate where the problem occurred.
 * 
 * @author Emanuel Rabina
 */
public aspect ExceptionWrapper {

	/**
	 * Catches exceptions raised during the startup phase of the game,
	 * re-throwing them as a {@link StartupException} so that any errors here
	 * can be traced to startup.
	 * 
	 * @throws StartupException
	 */
	void around() throws StartupException:
		execution(private static void Main.startup()) {

		try {
			proceed();
		}
		catch (Exception ex) {
			throw new StartupException("An error occurred during Startup", ex);
		}
	}

	/**
	 * Catches exceptions raised during the gameplay phase of the game,
	 * re-throwing them as a {@link GameplayException} so that any errors here
	 * can be traced to gameplay.
	 * 
	 * @throws GameplayException
	 */
	void around() throws GameplayException:
		execution(private static void Main.gameplay()) {

		try {
			proceed();
		}
		catch (Exception ex) {
			throw new GameplayException("An error occurred during Gameplay", ex);
		}
	}

	/**
	 * Catches exceptions raised during the shutdown phase of the game.
	 * 
	 * @throws ShutdownException
	 */
	void around() throws ShutdownException:
		execution(private static void Main.shutdown()) {

		try {
			proceed();
		}
		catch (Exception ex) {
			throw new ShutdownException("An error occurred during Shutdown", ex);
		}
	}
}
