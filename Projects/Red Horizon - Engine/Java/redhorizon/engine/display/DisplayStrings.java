
// =====================================
// Scanner's Java - Display strings keys
// =====================================

package redhorizon.engine.display;

import redhorizon.strings.StringsKey;

/**
 * Enumeration of strings keys used by the display package.
 * 
 * @author Emanuel Rabina
 */
public enum DisplayStrings implements StringsKey {

	// Shell/Window title
	GAMEWIN_WINDOW_TITLE,
	GAMEWIN_MENU_OPTIONS,

	// Display/Windowing errors
	DISPLAY_UNSUPPORTED_FULLSCREEN,
	DISPLAY_UNSUPPORTED_FULLSCREEN_RESOLUTION;

	private static final String RESOURCEBUNDLE_ENGINE_DISPLAY = "Game-Engine_Display";

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

		return RESOURCEBUNDLE_ENGINE_DISPLAY;
	}
}
