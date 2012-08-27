
package redhorizon.launcher.tasks;

/**
 * Interface for actions to be performed while the splash screen is open.
 * 
 * @author Emanuel Rabina
 */
public interface SplashScreenTask {

	/**
	 * Perform the action.
	 */
	public void doTask();

	/**
	 * Text to display on the splash screen while the task is being performed.
	 * 
	 * @return Some text for display on the splash screen.
	 */
	public String taskText();
}
