
// =====================================
// Scanner's Java - Player control class
// =====================================

package redhorizon.engine.input;

import redhorizon.engine.Player;
import redhorizon.engine.display.CursorTypes;
import redhorizon.engine.display.GameWindow;
import redhorizon.geometry.Point2D;
import redhorizon.geometry.Rectangle2D;
/*import redhorizon.game.hud.SelectionBox;
import redhorizon.game.objects.units.Structure;
import redhorizon.game.objects.units.Unit;
import redhorizon.media.Video;*/
import redhorizon.scenegraph.SceneManager;
import redhorizon.scenegraph.Selectable;
//import redhorizon.settings.Settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Just like how the graphics engine has a camera, and the audio engine a
 * listener, this is the input engine's interface into the game world.
 * 
 * @author Emanuel Rabina
 */
public class Controller {

	/**
	 * Possible modes the user can be in which affects the mouse cursor.
	 */
	private enum Modes {

		DEFAULT,
		SELECTED_UNIT,
		SELECTED_UNITS,
		SELECTED_STRUCTURE;
	}

	// Mouse related
	private Point2D clickorigin;
	private boolean clickdown;
	private Modes mode = Modes.DEFAULT;

	// Movement related
	private static int tickratexy;

	// Selection groups
	private final ArrayList<Selectable> selected = new ArrayList<Selectable>();

	// Game interfaces
	private Player player;
	private GameWindow gamewindow;

	/**
	 * Constructor, sets the controller to it's default settings.
	 */
	public Controller() {

		// Set the scroll speed
//		tickratexy = Integer.parseInt(Settings.getSetting(EngineSettingsKeys.SCROLLSPEEDXY));
	}

	/**
	 * Mouse click down at the specified window co-ordinates.
	 * 
	 * @param clickx X window co-ordinate of the click relative to the display.
	 * @param clicky Y window co-ordinate of the click relative to the display.
	 */
	void mouseClickDown(int clickx, int clicky) {

		clickdown = true;
		clickorigin = new Point2D(clickx, clicky).add(player.getPosition());

		// Reset the selection box area
//		SelectionBox.currentSelectionBox().setOrigin(clickorigin);
	}

	/**
	 * Mouse click up.
	 */
	void mouseClickUp() {

		clickdown = false;
		clickorigin = null;
		for (Selectable select: selected) {
			select.deselect();
		}
		selected.clear();

/*		SelectionBox selectionbox = SelectionBox.currentSelectionBox();
		Rectangle2D selectionarea = selectionbox.getSelectionArea();

		// Single-point selection
		if (selectionarea.width() == 0 && selectionarea.height() == 0) {
			Selectable object = scenemanager.getObjectAt(selectionarea.center());
			if (object == null) {
				mode = Modes.DEFAULT;
			}
			else {
				object.select();
				selected.add(object);
				if (object instanceof Structure) {
					mode = Modes.SELECTED_STRUCTURE;
				}
				else if (object instanceof Unit) {
					mode = Modes.SELECTED_UNIT;
				}
			}
		}

		// Multi-box selection
		else {
			List<Selectable> objects = scenemanager.getObjectsAt(selectionarea);
			if (objects.size() == 0) {
				mode = Modes.DEFAULT;
			}
			else {
				for (Iterator<Selectable> selectedobjects = objects.iterator(); selectedobjects.hasNext(); ) {
					Selectable select = selectedobjects.next();
					if (select instanceof Unit) {
						select.select();
					}
					else {
						selectedobjects.remove();
					}
				}
				selected.addAll(objects);
				mode = Modes.SELECTED_UNITS;
			}
		}

		selectionbox.clearSelection();

		// Update the cursor
		switch (mode) {
		case DEFAULT:
			gamewindow.setCursor(CursorTypes.DEFAULT);
			break;
		case SELECTED_UNIT:
		case SELECTED_UNITS:
			gamewindow.setCursor(CursorTypes.COMMAND_MOVE);
			break;
		case SELECTED_STRUCTURE:
		}
*/	}

	/**
	 * Mouse movement to the specified game world co-ordinates.
	 * 
	 * @param coordx X-axis window co-ordinates where the movement was made.
	 * @param coordy Y-axis window co-ordinates where the movement was made.
	 */
	void mouseMove(int coordx, int coordy) {

		mouseMove(new Point2D(coordx, coordy).add(player.getPosition()));
	}

	/**
	 * Mouse movement to the specified game world co-ordinates.
	 * 
	 * @param coords World co-ordinates at where the movement was made.
	 */
	void mouseMove(Point2D coords) {

		// Click & drag
		if (clickdown) {
			Point2D diff = coords.difference(clickorigin);
//			SelectionBox.currentSelectionBox().setSelection(diff.getX(), diff.getY());
		}

		// Normal movement
		else {
			Selectable selectable = SceneManager.currentSceneManager().getObjectAt(coords);
			if (selectable != null) {
				switch (mode) {
				case DEFAULT:
				case SELECTED_UNIT:
				case SELECTED_UNITS:
				case SELECTED_STRUCTURE:
					gamewindow.setCursor(CursorTypes.COMMAND_SELECT);
				}
			}
			else {
				switch (mode) {
				case DEFAULT:
					gamewindow.setCursor(CursorTypes.DEFAULT);
					break;
				case SELECTED_UNIT:
				case SELECTED_UNITS:
					gamewindow.setCursor(CursorTypes.COMMAND_MOVE);
					break;
				case SELECTED_STRUCTURE:
					gamewindow.setCursor(CursorTypes.DEFAULT);
					break;
				}
			}
		}
	}

	/**
	 * Moves the Player parts by the XYZ values specified.
	 * 
	 * @param movex Amount to move along the X axis.
	 * @param movey Amount to move along the Y axis.
	 */
	private void move(int movex, int movey) {

		player.setPosition(player.getPosition().add(movex, movey, 0));
	}

	/**
	 * Moves the player view down, along the negative Y axis.
	 */
	void moveDown() {

		move(0, -tickratexy);
	}

	/**
	 * Moves the player view left, along the negative X axis.
	 */
	void moveLeft() {

		move(-tickratexy, 0);
	}

	/**
	 * Moves the player view right, along the positve X axis.
	 */
	void moveRight() {

		move(tickratexy, 0);
	}

	/**
	 * Moves the player view up, along the positive Y axis.
	 */
	void moveUp() {

		move(0, tickratexy);
	}

	/**
	 * Sets the current instance of the <tt>GameWindow</tt>.
	 * 
	 * @param gamewindow The current game window.
	 */
	public void setGameWindow(GameWindow gamewindow) {

		this.gamewindow = gamewindow;
	}

	/**
	 * Sets the current <tt>Player</tt> instance.
	 * 
	 * @param player The current player.
	 */
	public void setPlayer(Player player) {

		this.player = player;
	}

	/**
	 * Skips the currently playing video (if any).
	 */
	void skipVideo() {

//		Video currentvid = Video.currentPlayingVideo();
//		if (currentvid != null) {
//			currentvid.stop();
//		}
	}
}
