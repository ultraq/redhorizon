
// ===========================================
// Scanner's Java - Main game controller class
// ===========================================

package redhorizon;

import redhorizon.engine.GameEngine;
import redhorizon.filemanager.FileManager;
import redhorizon.game.mission.CampaignFinder;
import redhorizon.launcher.ErrorMessageSWT;
import redhorizon.launcher.LauncherStringsKeys;
import redhorizon.launcher.MainMenu;
import redhorizon.launcher.MainMenuSWT;
import redhorizon.launcher.SplashScreen;
import redhorizon.launcher.SplashScreenAction;
import redhorizon.logging.Phase;
import redhorizon.media.MediaManager;
import redhorizon.strings.Strings;

import nz.net.ultraq.common.preferences.PreferencesFactory;

/**
 * The main class responsible for starting the game and controlling the flow of
 * events from startup to shutdown.  Also contains a few global program strings.
 * 
 * @author	Emanuel Rabina
 * @email	LightninUltraq@Netscape.net
 * @web		http://www.ultraq.net.nz/
 * @version 0.29 (dev)
 */
public final class Main {

	// Credits, version, etc
	private static final String name    = "Red Horizon";
	private static final String author  = "Emanuel Rabina";
	private static final String version = "0.29 (dev)";
	private static final String year    = "2008";

	// Set the application name to use the Preferences API, game window
	static {
		System.setProperty("nz.net.ultraq.common.preferences.appname", name);
		System.setProperty("redhorizon.engine.display.appname", name);
	}

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private Main() {
	}

	/**
	 * Main method; gets the ball rolling.  From here, the game is divided into
	 * phases, each with their own little steps:
	 * <ul>
	 * <li> - game startup</li>
	 * <li> - game play</li>
	 * <li> - game shutdown</li>
	 * </ul>
	 * 
	 * @param args Command-line parameters (if any).
	 */
	public static void main(String[] args) {

		Thread.currentThread().setName(getName() + " - " + getVersion() + ", " + getYear());
		int exitcode = 0;

		try {
			startup();
			gameplay();
			shutdown();
		}
		catch(Exception ex) {
			new ErrorMessageSWT(ex);
			exitcode = 1;
		}
		catch(Error er) {
			System.out.println(er.toString());
			er.printStackTrace();
			exitcode = 2;
		}

		System.exit(exitcode);
	}

	/**
	 * Returns the author string for this game.
	 * 
	 * @return "Emanuel Rabina"
	 */
	public static String getAuthor() {

		return author;
	}

	/**
	 * Returns the name of the game.
	 * 
	 * @return "Red Horizon"
	 */
	public static String getName() {

		return name;
	}

	/**
	 * Returns the version number of this game.
	 * 
	 * @return Current version, eg: "0.1"
	 */
	public static String getVersion() {

		return version;
	}

	/**
	 * Returns the year in which this project was conceived.
	 * 
	 * @return Year of this build, eg: "2007"
	 */
	public static String getYear() {

		return year;
	}

	/**
	 * Startup phase: loads basic settings, ensures existence of needed files,
	 * and searches for any custom content.
	 */
	@Phase("Startup")
	private static void startup() {

		// Display splash screen
		SplashScreen.createSplashScreen(

			// Start file & resource managers
			new SplashScreenAction() {
				public void action() {
					FileManager.init();
					MediaManager.init();
				}
				public String getActionText() {
					return Strings.getText(LauncherStringsKeys.SPLASH_LOAD_CORE);
				}
			},

			// Load current game data
			new SplashScreenAction() {
				public void action() {
					
				}
				public String getActionText() {
					return Strings.getText(LauncherStringsKeys.SPLASH_LOAD_STATE);
				}
			},

			// Search for available campaigns/mods
			new SplashScreenAction() {
				public void action() {
					CampaignFinder.findCampaigns();
				}
				public String getActionText() {
					return Strings.getText(LauncherStringsKeys.SPLASH_LOAD_CAMPAIGNS);
				}
			}
		);
	}

	/**
	 * The gameplay phase: starts the {@link GameEngine} thread.  From here the
	 * main menu and gameplay loop are controlled.
	 */
	@Phase("Gameplay")
	private static void gameplay() {

		// Maintain gameplay loop until exit is selected
		while (true) {

			// Display the main menu
			MainMenu mainmenu = new MainMenuSWT();
			if (mainmenu.exitCode()) {
				break;
			}

			// Delegate to the game engine
			GameEngine.start();
		}
	}

	/**
	 * Shutdown phase: saves any settings changes to disk, unloads all leftover
	 * content from memory.
	 */
	@Phase("Shutdown")
	private static void shutdown() {

		// Free-up resources taken by file and media data
		FileManager.close();
		MediaManager.close();
	}
}
