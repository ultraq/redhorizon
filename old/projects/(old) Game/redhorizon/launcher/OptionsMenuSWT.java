
// ===============================
// Scanner's Java - Options window
// ===============================

package redhorizon.launcher;

import redhorizon.Main;
import redhorizon.strings.Strings;
import redhorizon.utilities.SWTLayouts;
import redhorizon.utilities.SWTLayouts.HAlign;
import redhorizon.utilities.SWTLayouts.VAlign;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Game options are available and configurable through this window.  The options
 * will be divided into tabs, and in each tab the user can change options which
 * will affect the game.  In-game options will also be made available through
 * here, accessible only when the game engine is up and running.
 * 
 * @author Emanuel Rabina
 */
public class OptionsMenuSWT extends OptionsMenu {

	private final Shell shell;
	private final TabFolder tabfolder;

	/**
	 * Constructor, creates the window.
	 */
	public OptionsMenuSWT() {

		Display display = Display.getDefault();

		// New game background image
		Image raven = new Image(display, "Resources/Launcher/Splash_Raven.png");
		Image title = new Image(display, "Resources/Launcher/Text_OptionsMenu.png");
		Image image = new Image(display, 500, 500);
		GC gc = new GC(image);
		gc.drawImage(raven, 0, 35, 500, 100, 0, 0, 500, 100);
		gc.drawImage(title, 396, 83);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 100, 500, 241);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 341, 500, 50);

		// Window
		shell = new Shell(display, SWT.CLOSE | SWT.MIN | SWT.TITLE);
		shell.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_WINDOW_TITLE) + " - " + Main.getName());
		shell.setLayout(SWTLayouts.createLayoutShell());

		// Title image
		Label titleimg = new Label(shell, SWT.NONE);
		titleimg.setBackgroundImage(image);
		titleimg.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,500,100)));

		// Horizontal separator
		Label bar1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		bar1.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

		// Configuration group
		Composite config = new Composite(shell, SWT.NONE);
		config.setLayout(SWTLayouts.createLayoutGroup());
		config.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

			// Tab folder for tab items
			tabfolder = new TabFolder(config, SWT.TOP);
			tabfolder.setLayout(SWTLayouts.createLayoutInternal());
			tabfolder.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,200),
					HAlign.FILL, VAlign.NONE, true, false));

			// Set-up the widgets of each tab
			initGameTab();
			initVideoTab();
			initAudioTab();
			initControlsTab();
			initDevTab();


		// Horizontal separator
		Label bar2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		bar2.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

		// Window controls group
		Composite controls = new Composite(shell, SWT.NONE);
		controls.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		controls.setBackgroundMode(SWT.INHERIT_FORCE);
		controls.setLayout(SWTLayouts.createLayoutGroup(4, false));
		controls.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

			// Blank area?
			Label blank = new Label(controls, SWT.NONE);
			blank.setLayoutData(SWTLayouts.createLayoutData(true, true));

			// OK button button
			Button okbutton = new Button(controls, SWT.PUSH);
			okbutton.setText(Strings.getText(LauncherStringsKeys.BUTTON_OK));
			okbutton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					okSelected();
				}
			});
			okbutton.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,76,23)));

			// Cancel button
			Button cancelbutton = new Button(controls, SWT.PUSH);
			cancelbutton.setText(Strings.getText(LauncherStringsKeys.BUTTON_CANCEL));
			cancelbutton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					cancelSelected();
				}
			});
			cancelbutton.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,76,23)));

			// Apply button
			Button applybutton = new Button(controls, SWT.PUSH);
			applybutton.setText(Strings.getText(LauncherStringsKeys.BUTTON_APPLY));
			applybutton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					applySelected();
				}
			});
			applybutton.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,76,23)));


		// Open the window
		shell.pack();
		shell.open();

		// Don't hog CPU cycles
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		// Dispose of programmer-created resources
		gc.dispose();
		image.dispose();
		raven.dispose();
		title.dispose();
	}

	/**
	 * Applies the settings in their current state, but doesn't close the
	 * window.
	 */
	private void applySelected() {

		applySettings();
	}

	/**
	 * Closes the Options GUI and abandons any changes since the last save.
	 */
	private void cancelSelected() {

		restoreSettings();
		shell.dispose();
	}

	/**
	 * Sets-up the widgets found in the Audio Options tab.
	 */
	private void initAudioTab() {

		TabItem audiotab = new TabItem(tabfolder, SWT.NONE);
		audiotab.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_AUDTAB_TITLE));
		Composite audiotabcon = new Composite(tabfolder, SWT.NONE);
		audiotabcon.setLayout(SWTLayouts.createLayoutInternal(2, false));
		audiotab.setControl(audiotabcon);

		// Volume controls group
		Group volgroup = new Group(audiotabcon, SWT.NONE);
		volgroup.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_AUDTAB_VOLUME));
		volgroup.setLayout(SWTLayouts.createLayoutInternal(3, false));
		volgroup.setLayoutData(SWTLayouts.createLayoutData(false, false));

			// Master volume control
			Label mainvol = new Label(volgroup, SWT.LEFT);
			mainvol.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_AUDTAB_VOLUME_MAIN));
			mainvol.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));

			final Scale mainvolscale = new Scale(volgroup, SWT.HORIZONTAL);
			mainvolscale.setMinimum(VOLUME_MIN);
			mainvolscale.setMaximum(VOLUME_MAX);
			mainvolscale.setSelection((int)(mainvolume * 100));
			mainvolscale.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,19)));

			final Label mainvolval = new Label(volgroup, SWT.RIGHT);
			mainvolval.setText(Strings.floatToPercent(mainvolume));
			mainvolval.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));

			mainvolscale.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					mainvolval.setText(Strings.floatToPercent(mainvolscale.getSelection() / 100f));
					mainvolume = Strings.percentToFloat(mainvolval.getText());
				}
			});

			// HUD/EVA volume control
			Label hudvol = new Label(volgroup, SWT.LEFT);
			hudvol.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_AUDTAB_VOLUME_HUD));
			hudvol.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));
			hudvol.setEnabled(false);

			final Scale hudvolscale = new Scale(volgroup, SWT.HORIZONTAL);
			hudvolscale.setMinimum(VOLUME_MIN);
			hudvolscale.setMaximum(VOLUME_MAX);
			hudvolscale.setSelection((int)(hudvolume * 100));
			hudvolscale.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,19)));
			hudvolscale.setEnabled(false);

			final Label hudvolval = new Label(volgroup, SWT.RIGHT);
			hudvolval.setText(Strings.floatToPercent(hudvolume));
			hudvolval.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));
			hudvolval.setEnabled(false);

			hudvolscale.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					hudvolval.setText(Strings.floatToPercent(hudvolscale.getSelection() / 100f));
					hudvolume = Strings.percentToFloat(hudvolval.getText());
				}
			});

			// Music volume control
			Label musvol = new Label(volgroup, SWT.LEFT);
			musvol.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_AUDTAB_VOLUME_MUS));
			musvol.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));
			musvol.setEnabled(false);

			final Scale musvolscale = new Scale(volgroup, SWT.HORIZONTAL);
			musvolscale.setMinimum(VOLUME_MIN);
			musvolscale.setMaximum(VOLUME_MAX);
			musvolscale.setSelection((int)(musvolume * 100));
			musvolscale.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,19)));
			musvolscale.setEnabled(false);

			final Label musvolval = new Label(volgroup, SWT.RIGHT);
			musvolval.setText(Strings.floatToPercent(musvolume));
			musvolval.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));
			musvolval.setEnabled(false);

			musvolscale.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					musvolval.setText(Strings.floatToPercent(musvolscale.getSelection() / 100f));
					musvolume = Strings.percentToFloat(musvolval.getText());
				}
			});

			// Sound-effects volume control
			Label sfxvol = new Label(volgroup, SWT.LEFT);
			sfxvol.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_AUDTAB_VOLUME_SFX));
			sfxvol.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));
			sfxvol.setEnabled(false);

			final Scale sfxvolscale = new Scale(volgroup, SWT.HORIZONTAL);
			sfxvolscale.setMinimum(VOLUME_MIN);
			sfxvolscale.setMaximum(VOLUME_MAX);
			sfxvolscale.setSelection((int)(sfxvolume * 100));
			sfxvolscale.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,19)));
			sfxvolscale.setEnabled(false);

			final Label sfxvolval = new Label(volgroup, SWT.RIGHT);
			sfxvolval.setText(Strings.floatToPercent(sfxvolume));
			sfxvolval.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));
			sfxvolval.setEnabled(false);

			sfxvolscale.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					sfxvolval.setText(Strings.floatToPercent(sfxvolscale.getSelection() / 100f));
					sfxvolume = Strings.percentToFloat(sfxvolval.getText());
				}
			});

			// Video volume control
			Label vidvol = new Label(volgroup, SWT.LEFT);
			vidvol.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_AUDTAB_VOLUME_VID));
			vidvol.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));
			vidvol.setEnabled(false);

			final Scale vidvolscale = new Scale(volgroup, SWT.HORIZONTAL);
			vidvolscale.setMinimum(VOLUME_MIN);
			vidvolscale.setMaximum(VOLUME_MAX);
			vidvolscale.setSelection((int)(vidvolume * 100));
			vidvolscale.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,19)));
			vidvolscale.setEnabled(false);

			final Label vidvolval = new Label(volgroup, SWT.RIGHT);
			vidvolval.setText(Strings.floatToPercent(vidvolume));
			vidvolval.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));
			vidvolval.setEnabled(false);

			vidvolscale.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					vidvolval.setText(Strings.floatToPercent(vidvolscale.getSelection() / 100f));
					vidvolume = Strings.percentToFloat(vidvolval.getText());
				}
			});
	}

	/**
	 * Sets-up the widgets found in the Controls tab.
	 */
	private void initControlsTab() {

		TabItem contab = new TabItem(tabfolder, SWT.NONE);
		contab.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_CONTAB_TITLE));
		Composite contabcon = new Composite(tabfolder, SWT.NONE);
		contabcon.setLayout(SWTLayouts.createLayoutInternal());
		contab.setControl(contabcon);
	}

	/**
	 * Sets-up the widgets found in the Development tab.
	 */
	private void initDevTab() {

		TabItem devtab = new TabItem(tabfolder, SWT.NONE);
		devtab.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_DEVTAB_TITLE));
		Composite devtabcon = new Composite(tabfolder, SWT.NONE);
		devtabcon.setLayout(SWTLayouts.createLayoutInternal());
		devtab.setControl(devtabcon);

		// General controls group
		Group generalgroup = new Group(devtabcon, SWT.NONE);
		generalgroup.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_DEVTAB_GENERAL));
		generalgroup.setLayout(SWTLayouts.createLayoutInternal());

			// Enable debug tickbox
			final Button debugmode = new Button(generalgroup, SWT.CHECK);
			debugmode.setSelection(debug);
			debugmode.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_DEVTAB_GENERAL_DEBUG));
			debugmode.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					debug = debugmode.getSelection();
				}
			});
			debugmode.setLayoutData(SWTLayouts.createLayoutData(HAlign.LEFT, VAlign.NONE));
			debugmode.setEnabled(false);
	}

	/**
	 * Sets-up the widgets found in the Game Options tab.
	 */
	private void initGameTab() {

		TabItem gametab = new TabItem(tabfolder, SWT.NONE);
		gametab.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_GAMETAB_TITLE));
		Composite gametabcon = new Composite(tabfolder, SWT.NONE);
		gametabcon.setLayout(SWTLayouts.createLayoutInternal());
		gametab.setControl(gametabcon);

		// Map scrolling group
		Group scrollgroup = new Group(gametabcon, SWT.NONE);
		scrollgroup.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_GAMETAB_SCROLL));
		scrollgroup.setLayout(SWTLayouts.createLayoutInternal(3, false));
		scrollgroup.setLayoutData(SWTLayouts.createLayoutData(false, false));

			// XY scrolling
			Label xyscroll = new Label(scrollgroup, SWT.LEFT);
			xyscroll.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_GAMETAB_SCROLL_XY));
			xyscroll.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));

			final Scale xyscrollscale = new Scale(scrollgroup, SWT.HORIZONTAL);
			xyscrollscale.setMinimum(SCROLLXY_MIN);
			xyscrollscale.setMaximum(SCROLLXY_MAX);
			xyscrollscale.setSelection(scrollxy);
			xyscrollscale.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,19)));

			final Label xyscrollval = new Label(scrollgroup, SWT.RIGHT);
			xyscrollval.setText(scrollxy + "px");
			xyscrollscale.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					scrollxy = xyscrollscale.getSelection();
					xyscrollval.setText(scrollxy + "px");
				}
			});
			xyscrollval.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,30,SWT.DEFAULT),
					HAlign.FILL, VAlign.BOTTOM, true, true));

			// Z scrolling
			Label zscroll = new Label(scrollgroup, SWT.LEFT);
			zscroll.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_GAMETAB_SCROLL_Z));
			zscroll.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.BOTTOM, true, true));

			final Scale zscrollscale = new Scale(scrollgroup, SWT.HORIZONTAL);
			zscrollscale.setMinimum(SCROLLZ_MIN);
			zscrollscale.setMaximum(SCROLLZ_MAX);
			zscrollscale.setSelection(scrollz);
			zscrollscale.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,19)));

			final Label zscrollval = new Label(scrollgroup, SWT.RIGHT);
			zscrollval.setText(scrollz + "x");
			zscrollscale.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					scrollz = zscrollscale.getSelection();
					zscrollval.setText(scrollz + "x");
				}
			});
			zscrollval.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,30,SWT.DEFAULT),
					HAlign.FILL, VAlign.BOTTOM, true, true));		
			zscrollscale.setEnabled(false);
	}

	/**
	 * Sets-up the widgets found in the Video Options tab.
	 */
	private void initVideoTab() {

		TabItem videotab = new TabItem(tabfolder, SWT.NONE);
		videotab.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_VIDTAB_TITLE));
		Composite videotabcon = new Composite(tabfolder, SWT.NONE);
		videotabcon.setLayout(SWTLayouts.createLayoutInternal());
		videotab.setControl(videotabcon);

		// Display type and resolution
		Group displaygroup = new Group(videotabcon, SWT.NONE);
		displaygroup.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_VIDTAB_MODE));
		displaygroup.setLayout(SWTLayouts.createLayoutInternal(4, false));

			// Fullscreen mode radio button
			final Button fullradio = new Button(displaygroup, SWT.RADIO);
			fullradio.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_VIDTAB_MODE_FULL));
			fullradio.setSelection(fullscreen);
			fullradio.setLayoutData(SWTLayouts.createLayoutData(
					new Rectangle(0,0,100,SWT.DEFAULT), HAlign.LEFT, VAlign.FILL));
			fullradio.setEnabled(false);

			// List of supported fullscreen resolutions
			final Combo fullreslist = new Combo(displaygroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			fullreslist.setItems(resolutions.toArray(new String[resolutions.size()]));
			fullreslist.select(resolutions.indexOf(fullscreenres));
			fullreslist.setLayoutData(SWTLayouts.createLayoutData(HAlign.FILL, VAlign.NONE, 3, 1));
			fullreslist.setEnabled(false);

			// Windowed mode radio button
			final Button winradio = new Button(displaygroup, SWT.RADIO);
			winradio.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_VIDTAB_MODE_WINDOW));
			winradio.setSelection(!fullscreen);
			winradio.setLayoutData(SWTLayouts.createLayoutData(
					new Rectangle(0,0,100,SWT.DEFAULT), HAlign.LEFT, VAlign.FILL));

			// Text areas for customizable window sizes
			final Text winreswidth = new Text(displaygroup, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
			winreswidth.setTextLimit(4);
			winreswidth.setText(windowedres.substring(0, windowedres.indexOf('x')));
			winreswidth.addListener(SWT.Verify, new Listener() {
				public void handleEvent(Event event) {
					// Ensure entered text is digits only
					if (event.text.length() > 1 && !Character.isDigit(event.text.charAt(0))) {
						event.doit = false;
					}
				}
			});
			winreswidth.addListener(SWT.FocusIn, new Listener() {
				public void handleEvent(Event event) {
					winreswidth.setSelection(0, winreswidth.getCharCount());
				}
			});
			winreswidth.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,25,SWT.DEFAULT)));

			Label winresx = new Label(displaygroup, SWT.NONE);
			winresx.setText("x");

			final Text winresheight = new Text(displaygroup, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
			winresheight.setTextLimit(4);
			winresheight.setText(windowedres.substring(windowedres.indexOf('x') + 1));
			winresheight.addListener(SWT.Verify, new Listener() {
				public void handleEvent(Event event) {
					// Ensure entered text is digits only
					if (event.text.length() > 1 && !Character.isDigit(event.text.charAt(0))) {
						event.doit = false;
					}
				}
			});
			winresheight.addListener(SWT.FocusIn, new Listener() {
				public void handleEvent(Event event) {
					winresheight.setSelection(0, winresheight.getCharCount());
				}
			});
			winresheight.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,25,SWT.DEFAULT)));

			winreswidth.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					windowedres = winreswidth.getText() + "x" + winresheight.getText();
				}
			});
			winresheight.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					windowedres = winreswidth.getText() + "x" + winresheight.getText();
				}
			});

		// Miscellaneous video options group
		Group miscgroup = new Group(videotabcon, SWT.NONE);
		miscgroup.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_VIDTAB_MISC));
		miscgroup.setLayout(SWTLayouts.createLayoutInternal(3, false));
		miscgroup.setLayoutData(SWTLayouts.createLayoutData(true, false));

			// Scanline opacity slider
			Label lineopaclabel = new Label(miscgroup, SWT.LEFT);
			lineopaclabel.setText(Strings.getText(LauncherStringsKeys.GAMEOPT_VIDTAB_MISC_LINES));
			lineopaclabel.setLayoutData(SWTLayouts.createLayoutData(HAlign.NONE, VAlign.BOTTOM, false, true));

			final Scale lineopacity = new Scale(miscgroup, SWT.HORIZONTAL);
			lineopacity.setMinimum(LINE_OPACITY_MIN);
			lineopacity.setMaximum(LINE_OPACITY_MAX);
			lineopacity.setSelection((int)(scanlines * 100));
			lineopacity.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,SWT.DEFAULT,19)));

			final Label lineopacval = new Label(miscgroup, SWT.RIGHT);
			lineopacval.setText(Strings.floatToPercent(scanlines));
			lineopacval.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,30,SWT.DEFAULT),
					HAlign.FILL, VAlign.BOTTOM, true, false));

			lineopacity.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					lineopacval.setText(Strings.floatToPercent(lineopacity.getSelection() / 100f));
					scanlines = Strings.percentToFloat(lineopacval.getText());
				}
			});
	}

	/**
	 * Closes the Options GUI and applies the settings in their current state.
	 */
	private void okSelected() {

		applySettings();
		shell.dispose();
	}
}
