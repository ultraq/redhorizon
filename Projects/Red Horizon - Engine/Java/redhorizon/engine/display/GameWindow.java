
// ======================================
// Scanner's Java - Interface for display
// ======================================

package redhorizon.engine.display;

import redhorizon.geometry.Area2D;

import nz.net.ultraq.common.preferences.Preferences;
import nz.net.ultraq.gui.Window;

import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Window/Canvas to which the graphics engine will be rendering to.
 * <p>
 * To use, the application name must be a system property mapped to the
 * following key:
 * <tt>redhorizon.engine.display.appname</tt>
 * <p>
 * The default implementation uses a windowed implementation based on SWT, but
 * custom implementations can be used via the standard Java SPI mechanism.  In
 * this case, the file in <tt>META-INF/services</tt> should have the same name
 * as the fully qualified name of this class
 * (<tt>redhorizon.engine.display.GameWindow</tt>).  The first entry found using
 * this method, which supports the user-requested mode (windowed/fullscreen)
 * will be used.
 * <p>
 * All implementations are required to support windowed mode as a minimum.
 * 
 * @author Emanuel Rabina
 */
public abstract class GameWindow implements Window {

	// GameWindow configuration
	private static final String WINDOW_TITLE = "Red Horizon Engine";
	private static final String CONFIGURATION_FILE = "GameWindow.properties";
	private static ServiceLoader<GameWindow> serviceloader;

	// Singleton GameWindow instance
	private static GameWindow gamewindow;

	// Window attributes
	protected final Properties config;

	/**
	 * Default constructor, loads the window configuration from the
	 * <tt>GameWindow.properties</tt> file.
	 */
	protected GameWindow() {

		config = new Properties();
		config.load(getClass().getClassLoader().getResourceAsStream(CONFIGURATION_FILE));
	}

	/**
	 * Returns a <tt>GameWindow</tt> suited to the requirements specified by the
	 * user preferences.
	 * 
	 * @param callback Engine to be notified of window events.
	 * @return New <tt>GameWindow</tt> implementation.
	 */
	public static synchronized GameWindow createGameWindow(DisplayCallback callback) {

		if (serviceloader == null) {
			serviceloader = ServiceLoader.load(GameWindow.class);
		}

		// Return the first implementation that supports the requested mode
		String mode = Preferences.get(DisplayPreferences.DISPLAY_MODE);
		boolean fullscreen = !mode.equals("window");

		for (GameWindow windowimpl: serviceloader) {
			if (fullscreen && windowimpl.isFullScreenSupported()) {
				gamewindow = windowimpl;
				gamewindow.setRenderingCallback(callback);
				gamewindow.setWindowTitle(WINDOW_TITLE);
				return windowimpl;
			}
		}

		// Use the default
		gamewindow = new GameWindowSWT();
		gamewindow.setRenderingCallback(callback);
		gamewindow.setWindowTitle(WINDOW_TITLE);

		return gamewindow;
	}

	/**
	 * Returns the currently running <tt>GameWindow</tt> instance.
	 * 
	 * @return The current game window, or <tt>null</tt> if there is no
	 * 		   current window.
	 */
	public static GameWindow currentGameWindow() {

		return gamewindow;
	}

	/**
	 * Returns the size of the rendering area.
	 * 
	 * @return Rendering area size, in pixels.
	 */
	public abstract Area2D getRenderingArea();

	/**
	 * Returns whether or not the implementation can support a full-screen mode.
	 * 
	 * @return <tt>true</tt> if it supports full-screen mode, <tt>false</tt>
	 * 		   otherwise.
	 */
	protected abstract boolean isFullScreenSupported();

	/**
	 * Requests that the mouse cursor stored under the given name be displayed
	 * as the current cursor.
	 * 
	 * @param cursortype The type of cursor to display.
	 */
	public abstract void setCursor(CursorTypes cursortype);

	/**
	 * Sets the inputhandler for receiving and dealing with input events such as
	 * mouse clicks/movement and keyboard presses.
	 * 
	 * @param inputhandler The object to handle input events.
	 */
	public abstract void setInputEventHandler(InputEventHandler inputhandler);

	/**
	 * Sets the inputhandler for rendering events defined by the
	 * <tt>DisplayCallback</tt> interface.
	 * 
	 * @param callback The object to be notified of significant display events.
	 */
	protected abstract void setRenderingCallback(DisplayCallback callback);

	/**
	 * Sets the winow title.
	 * 
	 * @param title Window title.
	 */
	protected abstract void setWindowTitle(String title);

	/**
	 * Sets that the implementation should render in full-screen mode.
	 * 
	 * @throws UnsupportedOperationException If full-screen mode is not
	 * 		   supported by the implementation.
	 */
	protected void useFullScreen() {

		throw new UnsupportedOperationException("Full-screen mode not supported.");
	}
}
