
package redhorizon.utilities.converter;

import redhorizon.launcher.SplashScreen;
import redhorizon.launcher.Window;
import redhorizon.launcher.tasks.LoadModRedAlertTask;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI version of the Red Horizon converter utility.
 * 
 * @author Emanuel Rabina
 */
public class ConverterUI extends Window {

	private static final Logger logger = LoggerFactory.getLogger(ConverterUI.class);

	/**
	 * Entry point for the Red Horizon converter utility.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		logger.info("Starting Converter UI...");

		// Initialize the program
		SplashScreen splashscreen = new SplashScreen(ConverterUI.class.getClassLoader()
				.getResourceAsStream("Launcher_SplashScreen.png"),
				"Red Horizon File Conversion Utility", "0.30");
		splashscreen.addTask(new LoadModRedAlertTask());
		splashscreen.open();

		ConverterUI converterui = new ConverterUI();
		converterui.open();
	}

	/**
	 * Constructor, creates a new converter window.
	 */
	public ConverterUI() {

		super(SWT.SHELL_TRIM);
	}
}
