
package redhorizon.utilities.converter.ui;

import redhorizon.launcher.tasks.LoadModRedAlertTask;
import redhorizon.ui.ApplicationWindow;
import redhorizon.ui.SplashScreen;
import static redhorizon.utilities.converter.ui.ConverterUIPreferences.*;

import nz.net.ultraq.preferences.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI version of the Red Horizon converter utility.
 * 
 * @author Emanuel Rabina
 */
public class ConverterUI extends ApplicationWindow {

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

		super(true);

		// Menu items
		Menu filemenu = menumanager.addMenu("&File");
		{
			MenuItem exititem = new MenuItem(filemenu, SWT.PUSH);
			exititem.setText("E&xit\tAlt+F4");
			exititem.setAccelerator(SWT.ALT | SWT.F4);
			exititem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					close();
				}
			});
		}

		// Splitter between file browser and app area
		SashForm sash = new SashForm(shell, SWT.HORIZONTAL | SWT.SMOOTH);
		sash.setLayout(new FillLayout());

		Composite left = new Composite(sash, SWT.NONE);
		left.setLayout(new FillLayout());

		TreeFileManager filemanager = new TreeFileManager(left);

		Composite right = new Composite(sash, SWT.NONE);
		right.setLayout(new FillLayout());
		new Label(right, SWT.NONE).setText("Right");

		sash.setWeights(new int[]{ 25, 75 });
	}

	/**
	 * Set the converter window to the last used position, otherwise center it
	 * on the screen.
	 */
	@Override
	protected void pack() {

		if (Preferences.getBoolean(WINDOW_MAXIMIZED)) {
			shell.setMaximized(true);
		}
		else {
			shell.setBounds(
					Preferences.getInt(WINDOW_BOUNDS_X),
					Preferences.getInt(WINDOW_BOUNDS_Y),
					Preferences.getInt(WINDOW_BOUNDS_WIDTH),
					Preferences.getInt(WINDOW_BOUNDS_HEIGHT)
			);
			if (!Preferences.preferenceExists(WINDOW_BOUNDS_X)) {
				super.pack();
			}
		}
	}
}
