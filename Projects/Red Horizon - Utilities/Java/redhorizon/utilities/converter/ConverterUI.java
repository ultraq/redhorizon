
package redhorizon.utilities.converter;

import redhorizon.launcher.SplashScreen;
import redhorizon.launcher.Window;
import redhorizon.launcher.tasks.LoadModRedAlertTask;
import static redhorizon.utilities.converter.ConverterUIPreferences.*;

import nz.net.ultraq.preferences.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
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

	/**
	 * Set the converter window to the last used position, otherwise center it
	 * on the screen.
	 */
	@Override
	protected void pack() {

		shell.setMaximized(Preferences.getBoolean(WINDOW_MAXIMIZED));
		if (!shell.getMaximized()) {
			shell.setBounds(
					Preferences.getInt(WINDOW_BOUNDS_X),
					Preferences.getInt(WINDOW_BOUNDS_Y),
					Preferences.getInt(WINDOW_BOUNDS_WIDTH),
					Preferences.getInt(WINDOW_BOUNDS_HEIGHT)
			);
			if (!Preferences.preferenceExists(WINDOW_BOUNDS_X)) {
				Rectangle displayarea = shell.getDisplay().getPrimaryMonitor().getBounds();
				Rectangle windowarea = shell.getBounds();
				shell.setBounds((displayarea.width - windowarea.width) / 2,
						(displayarea.height - windowarea.height) / 2,
						windowarea.width, windowarea.height);
			}
		}
	}
}
