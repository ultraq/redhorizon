
// ===============================
// Scanner's Java - Main game menu
// ===============================

package redhorizon.launcher;

import redhorizon.Main;
import redhorizon.strings.Strings;
import redhorizon.utilities.SWTLayouts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * The top-level menu for all of the game's options: new game, load, etc.  From
 * here the player is able to navigate to every available customizable aspect of
 * the game.
 * 
 * @author Emanuel Rabina
 */
class MainMenuSWT extends MainMenu {

	private final Shell shell;

	/**
	 * Constructor, creates the window, options.
	 */
	MainMenuSWT() {

		Display display = Display.getDefault();

		// Culled image for the menu title
		Image raven = new Image(display, "Resources/Launcher/Splash_Raven.png");
		Image title = new Image(display, "Resources/Launcher/Text_MainMenu.png");
		Image image = new Image(display, 320, 400);
		GC gc = new GC(image);
		gc.drawImage(raven, 90, 35, 320, 100, 0, 0, 320, 100);
		gc.drawImage(title, 241, 83);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 100, 320, 158);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 258, 320, 50);

		// Window
		shell = new Shell(display, SWT.TITLE | SWT.MIN);
		shell.setText(Strings.getText(LauncherStringsKeys.GAMEMENU_WINDOW_TITLE) + " - " + Main.getName());
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				exitcode = true;
			}
		});
		shell.setLayout(SWTLayouts.createLayoutShell());

		// Title image
		Label titleimg = new Label(shell, SWT.NONE);
		titleimg.setBackgroundImage(image);
		titleimg.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,320,100)));

		// Horizontal separator
		Label bar1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		bar1.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

		// Configuration group
		Composite config = new Composite(shell, SWT.NONE);
		config.setLayout(SWTLayouts.createLayoutGroup());
		config.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

			// New game button
			Button newgame = new Button(config, SWT.PUSH);
			newgame.setText(Strings.getText(LauncherStringsKeys.GAMEMENU_BUTTON_NEWGAME));
			newgame.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					newGameSelected();
				}
			});
			newgame.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,120,23),
					SWTLayouts.HAlign.CENTER, SWTLayouts.VAlign.NONE, true, false));

			// Load game button
			Button loadgame = new Button(config, SWT.PUSH);
			loadgame.setEnabled(false);
			loadgame.setText(Strings.getText(LauncherStringsKeys.GAMEMENU_BUTTON_LOADGAME));
			loadgame.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,120,23),
					SWTLayouts.HAlign.CENTER, SWTLayouts.VAlign.NONE, true, false));

			// Multiplayer game button
			Button multigame = new Button(config, SWT.PUSH);
			multigame.setEnabled(false);
			multigame.setText(Strings.getText(LauncherStringsKeys.GAMEMENU_BUTTON_MULTIPLAYER));
			multigame.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,120,23),
					SWTLayouts.HAlign.CENTER, SWTLayouts.VAlign.NONE, true, false));

			// Options button
			Button options = new Button(config, SWT.PUSH);
			options.setText(Strings.getText(LauncherStringsKeys.GAMEMENU_BUTTON_OPTIONS));
			options.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					optionsSelected();
				}
			});
			options.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,120,23),
					SWTLayouts.HAlign.CENTER, SWTLayouts.VAlign.NONE, true, false));

			// Exit button
			Button exit = new Button(config, SWT.PUSH);
			exit.setText(Strings.getText(LauncherStringsKeys.GAMEMENU_BUTTON_EXIT));
			exit.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					exitSelected();
				}
			});
			exit.setLayoutData(SWTLayouts.createLayoutData(new Rectangle(0,0,120,23),
					SWTLayouts.HAlign.CENTER, SWTLayouts.VAlign.NONE, true, false));


		// Horizontal separator
		Label bar2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		bar2.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

		// Window controls group
		Composite controls = new Composite(shell, SWT.NONE);
		controls.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		controls.setBackgroundMode(SWT.INHERIT_FORCE);
		controls.setLayout(SWTLayouts.createLayoutGroup(2, true));
		controls.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.FILL, SWTLayouts.VAlign.NONE));

			// Smaller font
			FontData smallfontdata = display.getSystemFont().getFontData()[0];
			smallfontdata.setHeight(smallfontdata.getHeight() - 1);
			Font smallfont = new Font(display, smallfontdata);

			// Credits
			Label credits = new Label(controls, SWT.LEFT);
			credits.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			credits.setFont(smallfont);
			credits.setText(Strings.getText(LauncherStringsKeys.WORD_COPYRIGHT, Main.getYear() + ", " + Main.getAuthor()));
			credits.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.LEFT, SWTLayouts.VAlign.NONE, true, false));

			// Version number
			Label version = new Label(controls, SWT.RIGHT);
			version.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			version.setFont(smallfont);
			version.setText(Strings.getText(LauncherStringsKeys.WORD_VERSION, Main.getVersion()));
			version.setLayoutData(SWTLayouts.createLayoutData(SWTLayouts.HAlign.RIGHT, SWTLayouts.VAlign.NONE));


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
		title.dispose();
		raven.dispose();
		smallfont.dispose();

		display.dispose();
	}

	/**
	 * Exit routine for when the Exit button is clicked/selected.  Stops the
	 * background music and closes the Main Menu GUI.
	 */
	private void exitSelected() {

		exitcode = true;
		shell.dispose();
	}

	/**
	 * Opens the New Game Menu when the New Game button is clicked/selected.
	 * Hides this menu in the interim, restored once the New Game Menu is
	 * closed.
	 */
	private void newGameSelected() {

		shell.setVisible(false);
		NewGameMenuSWT newgamemenu = new NewGameMenuSWT();

		// Close the menu if a game has been selected
		if (newgamemenu.exitCode()) {
			shell.dispose();
		}
		else {
			shell.setVisible(true);
		}
	}

	/**
	 * Opens the Options Menu when the Options button is clicked/selected.
	 * Hides this menu in the interim, restored once the Options Menu is closed.
	 */
	private void optionsSelected() {

		shell.setVisible(false);
		new OptionsMenuSWT();
		shell.setVisible(true);
	}
}
