
// =================================================
// Scanner's Java - Parent Unit instance abstraction
// =================================================

package redhorizon.game.objects.units;

import redhorizon.engine.scenegraph.Drawable;
import redhorizon.engine.scenegraph.SceneObject;
import redhorizon.engine.scenegraph.Selectable;
import redhorizon.game.hud.StatusBox;
import redhorizon.game.objects.Instance;
import redhorizon.misc.geometry.Rectangle2D;

/**
 * Top-level abstraction of an instance of the the Red Horizon unit type.
 * Represents instances of all sorts of units and defines the methods they need
 * to fulfill to become fully qualified game unit instances.<br>
 * <br>
 * This class also encompasses much of the Structure type.  Both units and
 * structures share a lot of similarities, and due to the use of the word 'unit'
 * to include structures in the same group as other units in the original Red
 * Alert configuration (<code>Rules.ini</code>), this class will remain the
 * parent abstraction for both types.
 * 
 * @author Emanuel Rabina
 * @param <M> The implementation type this instance is built from and
 * 			  references.
 */
public abstract class Unit<M extends UnitImpl<? extends Unit<M>>>
	extends SceneObject implements Instance, Drawable, Selectable {

	// Instance ID
	private static int instance = 0;

	// Drawable parts
	private boolean drawing;
	protected boolean selected;

	// Instance to implementation links
	protected final M impl;
	protected final String name;

	// Unit instance variables
	protected float hp;
	protected float heading;
//	private Vector3f vector;
//	SubFaction country;
//	Behaviour behaviour;
//	Trigger trigger;

	protected final StatusBox status;

	/**
	 * Constructor, links an instance to it's implementation.
	 * 
	 * @param impl <code>UnitImpl</code> that this instance stems from.
	 */
	protected Unit(M impl) {

		this.impl = impl;
		this.name = impl.name + acquireInstanceID();

		hp = impl.maxhp;
		status = new StatusBox(impl.footprint);
	}

	/**
	 * Atomically gets and increments the number-of-instances counter.
	 * 
	 * @return The next unused instance ID.
	 */
	private static synchronized int acquireInstanceID() {

		return instance++;
	}

	/**
	 * @inheritDoc
	 */
	public void deselect() {

		selected = false;
	}

	/**
	 * @inheritDoc
	 */
	public void draw() {

		drawing = true;
		status.erase();
	}

	/**
	 * @inheritDoc
	 */
	public void erase() {

		drawing = false;
		status.draw();
	}

	/**
	 * @inheritDoc
	 */
	public Rectangle2D getBoundingArea() {

		return impl.footprint;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isDrawing() {

		return drawing;
	}

	/**
	 * @inheritDoc
	 */
	public void select() {

		selected = true;
	}

	/**
	 * Sets the unit's current heading.
	 * 
	 * @param heading Degrees from the 12 o'clock position to have the unit
	 * 				  directed towards.
	 */
	protected void setHeading(float heading) {

		this.heading = heading;
	}

	/**
	 * Sets the unit's hitpoints to a percentage of it's max hitpoints
	 * (multiplies max HP by given <code>float</code> to obtain current HP).
	 * 
	 * @param percentage Amount to reduce HP to.
	 */
	protected void setHPPercentage(float percentage) {

		hp = impl.maxhp * percentage;
		status.setHPPercentage(percentage);
	}
}
