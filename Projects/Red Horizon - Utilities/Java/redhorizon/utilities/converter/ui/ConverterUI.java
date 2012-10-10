
package redhorizon.utilities.converter.ui;

import redhorizon.launcher.tasks.LoadModRedAlertTask;
import redhorizon.ui.ApplicationWindow;
import redhorizon.ui.SplashScreen;
import static redhorizon.utilities.converter.ui.ConverterUIPreferences.*;

import nz.net.ultraq.preferences.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * GUI version of the Red Horizon converter utility.
 * 
 * @author Emanuel Rabina
 */
public class ConverterUI extends ApplicationWindow {

	private static final Logger logger = LoggerFactory.getLogger(ConverterUI.class);

	private static final int MAX_HISTORY = 10;

	private final Menu filemenu;
	private final MenuItem exititem;
	private final ArrayList<Conversion> conversionhistory = new ArrayList<>(11);

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
	@SuppressWarnings("unchecked")
	public ConverterUI() {

		super(true);

		// Menu items
		filemenu = menumanager.addMenu("&File");
		{
			// New conversion menu
			MenuItem newitem = new MenuItem(filemenu, SWT.CASCADE);
			newitem.setText("&New...");
			Menu newmenu = new Menu(newitem);
			newitem.setMenu(newmenu);
			{
				// PNG (Multiple) -> SHP (Dune 2)
				MenuItem mpng2shpd2item = new MenuItem(newmenu, SWT.PUSH);
				mpng2shpd2item.setText("PNG (multiple) to SHP (Dune 2) conversion");
				mpng2shpd2item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						convert(ConversionFile.PNG_MULTIPLE, null, ConversionFile.SHP_D2, null);
					}
				});
			}

			// Display all recent conversion operations done by the user
			ArrayList<Conversion> conversions = (ArrayList<Conversion>)Preferences.getObject(CONVERSION_HISTORY);
			if (!conversions.isEmpty()) {
				for (Conversion conversion: conversions) {
					addToConversionHistory(conversion);
				}
			}

			new MenuItem(filemenu, SWT.SEPARATOR);

			// Exit
			exititem = new MenuItem(filemenu, SWT.PUSH);
			exititem.setText("E&xit\tAlt+F4");
			exititem.setAccelerator(SWT.ALT | SWT.F4);
			exititem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					close();
				}
			});

		}
	}

	/**
	 * Adds a recent conversion to the conversion history section of the file
	 * menu.
	 * 
	 * @param conversion
	 */
	private void addToConversionHistory(final Conversion conversion) {

		// Create a new menu item for the conversion
		if (conversionhistory.isEmpty()) {
			new MenuItem(filemenu, SWT.SEPARATOR, 1);
		}
		conversionhistory.add(0, conversion);

		final MenuItem historyitem = new MenuItem(filemenu, SWT.PUSH, 2);
		historyitem.setText(conversion.toString());
		historyitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				// Re-order the history to put the most recently clicked item to the top
				removeFromConversionHistory(conversion, historyitem);
				convert(conversion.source, conversion.inputfiles, conversion.target, conversion.outputfile);
			}
		});

		// Remove old conversions if the max history to keep is exceeded
		if (conversionhistory.size() > MAX_HISTORY) {
			conversionhistory.remove(MAX_HISTORY);
			filemenu.getItem(filemenu.indexOf(exititem) - 2).dispose();
		}
	}

	/**
	 * Start a conversion project with the specified parameters.
	 * 
	 * @param source
	 * @param inputfiles
	 * @param target
	 * @param outputfile
	 */
	private void convert(ConversionFile source, String[] inputfiles, ConversionFile target, String outputfile) {

		Conversion conversion = new Conversion(source, inputfiles, target, outputfile);

		// Create input panel
		Group inputgroup = new Group(shell, SWT.NONE);
		inputgroup.setText(source.name + " -> " + target.name);
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

	/**
	 * Removes an item from the conversion history.
	 * 
	 * @param conversion
	 * @param menuitem
	 */
	private void removeFromConversionHistory(Conversion conversion, MenuItem menuitem) {

		menuitem.dispose();
		conversionhistory.remove(conversion);
	}
}
