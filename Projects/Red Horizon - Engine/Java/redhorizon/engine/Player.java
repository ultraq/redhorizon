
// ================================================================
// Scanner's Java - Representation of the user's view into the game
// ================================================================

package redhorizon.engine;

import redhorizon.engine.audio.Listener;
import redhorizon.engine.graphics.Camera;
import redhorizon.engine.input.Controller;
import redhorizon.geometry.Point2D;
import redhorizon.geometry.Point3D;
import redhorizon.scenegraph.AbstractTransformable;

/**
 * A central control class for the {@link Camera}, {@link Listener}, and
 * {@link Controller} so that player input can be reflected in all these other
 * parts.
 * 
 * @author Emanuel Rabina
 */
public class Player extends AbstractTransformable {

	// Singleton player instance
	private static Player player;

	// Player properties
	private final Camera camera;
	private final Listener listener;
	private final Controller controller;
	private boolean movementlocked;

	/**
	 * Constructor, creates a new player tied to the given camera and listener.
	 * 
	 * @param camera	 Camera to tie to this player.
	 * @param listener	 Listener to tie to this player.
	 * @param controller Controller to tie to this player.
	 */
	Player(Camera camera, Listener listener, Controller controller) {

		this.camera     = camera;
		this.listener   = listener;
		this.controller = controller;

		player = this;
	}

	/**
	 * Centers the player on the given XY co-ordinates.  Allows refocussing on
	 * the given co-ordinates even if movement has been locked.
	 * 
	 * @param focus XY co-ordinates to center on.
	 */
	public void focus(Point2D focus) {

		setPosition(new Point3D(focus.getX(), focus.getY(), getPosition().getZ()));
	}

	/**
	 * Returns the singleton player instance.
	 * 
	 * @return The player object.
	 */
	public static Player getCurrentPlayer() {

		return player;
	}

	/**
	 * Returns the camera attached to this player.
	 * 
	 * @return The attached camera.
	 */
	public Camera getCamera() {

		return camera;
	}

	/**
	 * Returns the controller attached to this player.
	 * 
	 * @return The attached controller.
	 */
	public Controller getController() {

		return controller;
	}

	/**
	 * Returns the listener attached to this player.
	 * 
	 * @return The attached listener.
	 */
	public Listener getListener() {

		return listener;
	}

	/**
	 * Locks only the movement of the camera and listener parts, while allowing
	 * input commands to get through.
	 */
	public void lockMovement() {

		movementlocked = true;
	}

	/**
	 * Modifies the current position of the player.  This is only possible if
	 * player movement hasn't been restricted by {@link #lockMovement()}.
	 * 
	 * @param position The new position of the player.
	 */
	public void setPosition(Point3D position) {

		if (!movementlocked) {
			super.setPosition(position);
		}
	}

	/**
	 * Modifies the current orientation of the player.  This is only possible if
	 * player movement hasn't been restricted by {@link #lockMovement()}.
	 * 
	 * @param rotation The new rotation of the player.
	 */
	public void setRotation(float rotation) {

		if (!movementlocked) {
			super.setRotation(rotation);
		}
	}

	/**
	 * Unlock the movement aspect of the player, allowing free movement of the
	 * camera and listener once more.
	 */
	public void unlockMovement() {

		movementlocked = false;
	}
}
