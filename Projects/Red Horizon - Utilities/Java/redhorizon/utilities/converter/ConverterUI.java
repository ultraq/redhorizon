
package redhorizon.utilities.converter;

import redhorizon.launcher.SplashScreen;
import redhorizon.launcher.Window;
import redhorizon.launcher.tasks.LoadModRedAlertTask;

import org.eclipse.swt.SWT;

/**
 * GUI version of the Red Horizon converter utility.
 * 
 * @author Emanuel Rabina
 */
public class ConverterUI extends Window {

	/**
	 * Entry point for the Red Horizon converter utility.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Initialize the program
		SplashScreen splashscreen = new SplashScreen(ConverterUI.class.getClassLoader()
				.getResourceAsStream("Resources/Launcher_SplashScreen.png"),
				"Red Horizon File Conversion Utility", "1.0");
		splashscreen.addTask(new LoadModRedAlertTask());
		splashscreen.open();

		new ConverterUI();
	}

	/**
	 * Constructor, creates a new converter window.
	 */
	public ConverterUI() {

		super(SWT.SHELL_TRIM);
	}
}
