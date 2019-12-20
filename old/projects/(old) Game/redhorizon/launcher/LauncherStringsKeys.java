
// ======================================
// Scanner's Java - Launcher strings keys
// ======================================

package redhorizon.launcher;

import redhorizon.strings.StringsKey;

/**
 * Enumeration of keys to strings used for the Red Horizon launcher.
 * 
 * @author Emanuel Rabina
 */
public enum LauncherStringsKeys implements StringsKey {

	// Installation messages
	MISSING_GAME,

	// Splash screen messages
	SPLASH_LOAD_CORE,
	SPLASH_LOAD_CAMPAIGNS,
	SPLASH_LOAD_STATE,

	// Main menu window
	GAMEMENU_WINDOW_TITLE,
	GAMEMENU_BUTTON_NEWGAME,
	GAMEMENU_BUTTON_LOADGAME,
	GAMEMENU_BUTTON_MULTIPLAYER,
	GAMEMENU_BUTTON_OPTIONS,
	GAMEMENU_BUTTON_EXIT,

	// New game window
	GAMENEW_WINDOW_TITLE,
	GAMENEW_GROUP1_HEADER,
	GAMENEW_GROUP2_HEADER,
	GAMENEW_DESC_DETAILS,

	// Options window
	GAMEOPT_WINDOW_TITLE,

	GAMEOPT_GAMETAB_TITLE,
	GAMEOPT_GAMETAB_SCROLL,
	GAMEOPT_GAMETAB_SCROLL_XY,
	GAMEOPT_GAMETAB_SCROLL_Z,

	GAMEOPT_VIDTAB_TITLE,
	GAMEOPT_VIDTAB_MODE,
	GAMEOPT_VIDTAB_MODE_FULL,
	GAMEOPT_VIDTAB_MODE_WINDOW,
	GAMEOPT_VIDTAB_MISC,
	GAMEOPT_VIDTAB_MISC_LINES,

	GAMEOPT_AUDTAB_TITLE,
	GAMEOPT_AUDTAB_VOLUME,
	GAMEOPT_AUDTAB_VOLUME_MAIN,
	GAMEOPT_AUDTAB_VOLUME_HUD,
	GAMEOPT_AUDTAB_VOLUME_MUS,
	GAMEOPT_AUDTAB_VOLUME_SFX,
	GAMEOPT_AUDTAB_VOLUME_VID,

	GAMEOPT_CONTAB_TITLE,

	GAMEOPT_DEVTAB_TITLE,
	GAMEOPT_DEVTAB_GENERAL,
	GAMEOPT_DEVTAB_GENERAL_DEBUG,
	GAMEOPT_DEVTAB_THREADS,
	GAMEOPT_DEVTAB_THREADS_AUD,
	GAMEOPT_DEVTAB_THREADS_INPUT,
	GAMEOPT_DEVTAB_THREADS_VID,

	// Mix location messages
	MIXLOC_WINDOW_TITLE,
	MIXLOC_INSTRUCTIONS,
	MIXLOC_BYPASS,
	MIXLOC_PATH,
	MIXLOC_COPYING,
	MIXLOC_GET_PATH,
	MIXLOC_PATH_FAILED,

	// Dialog window headers
	WINDOW_MESSAGE,
	WINDOW_DIRECTORY,

	// Button text
	BUTTON_APPLY,
	BUTTON_BROWSE,
	BUTTON_CANCEL,
	BUTTON_OK,
	BUTTON_PLAY,

	// Single word combos
	WORD_COPYRIGHT,
	WORD_VERSION;

	private static final String RESOURCEBUNDLE_LAUNCHER = "Game_Launcher";

	/**
	 * @inheritDoc
	 */
	public String getKey() {

		return name();
	}

	/**
	 * @inheritDoc
	 */
	public String getResourceBundle() {

		return RESOURCEBUNDLE_LAUNCHER;
	}
}
