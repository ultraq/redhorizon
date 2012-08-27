
package redhorizon.launcher.tasks;

/**
 * Load all of the standard C&C Red Alert files as a mod.
 *
 * @author Emanuel Rabina
 */
public class LoadModRedAlertTask extends LoadModTask {

	private static final String RED_ALERT_MOD_DIRECTORY = "_Red Alert";

	/**
	 * Constructor, create the load Red Alert task.
	 */
	public LoadModRedAlertTask() {

		super(RED_ALERT_MOD_DIRECTORY);
	}
}
