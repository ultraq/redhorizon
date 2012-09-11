
package redhorizon.utilities.converter.ui;

import redhorizon.launcher.tasks.LoadModRedAlertTask;
import redhorizon.ui.ApplicationWindow;
import redhorizon.ui.SplashScreen;
import static redhorizon.utilities.converter.ui.ConverterUIPreferences.*;

import nz.net.ultraq.preferences.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;

/**
 * GUI version of the Red Horizon converter utility.
 * 
 * @author Emanuel Rabina
 */
public class ConverterUI extends ApplicationWindow {

	private static final Logger logger = LoggerFactory.getLogger(ConverterUI.class);

	private final Group recentconversions;

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
			MenuItem newitem = new MenuItem(filemenu, SWT.CASCADE);
			newitem.setText("&New...");
			Menu newmenu = new Menu(newitem);
			newitem.setMenu(newmenu);
			{
				MenuItem mpng2shpd2item = new MenuItem(newmenu, SWT.PUSH);
				mpng2shpd2item.setText("PNG (multiple) to SHP (Dune 2) conversion");
				mpng2shpd2item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						convert(ConversionFile.PNG_MULTIPLE, null, ConversionFile.SHP_D2, null);
					}
				});
			}

			new MenuItem(filemenu, SWT.SEPARATOR);

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

		// Display all recent conversion operations done by the user
		recentconversions = new Group(shell, SWT.NONE);
		recentconversions.setText("Conversion history");

		FillLayout historylayout = new FillLayout();
		historylayout.marginWidth = 5;
		historylayout.marginHeight = 5;
		historylayout.spacing = 5;
		recentconversions.setLayout(historylayout);

		@SuppressWarnings("unchecked")
		Deque<Conversion> conversionhistory = (Deque<Conversion>)Preferences.getObject(CONVERSION_HISTORY);
		for (final Conversion conversion: conversionhistory) {
			Link link = new Link(recentconversions, SWT.NONE);
			link.setText(conversion.toString());
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					convert(conversion.source, conversion.inputfiles, conversion.target, conversion.outputfile);
				}
			});
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

		// Clear the recent conversions history
		if (!recentconversions.isDisposed()) {
			recentconversions.dispose();
		}

		// Create input panel
		Group inputgroup = new Group(shell, SWT.NONE);
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
