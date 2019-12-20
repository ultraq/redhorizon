
// ==================================
// Scanner's Java - Vehicle interface
// ==================================

package redhorizon.game.objects.units;

import redhorizon.media.Image;

import javax.media.opengl.GL;

/**
 * Interface and type identifier for <code>Vehicle</code> type game objects.
 * Used to bring together game object implementations from game object
 * instances.
 * 
 * @author Emanuel Rabina
 */
public class Vehicle extends Unit<VehicleImpl> {

	private int todraw;

	/**
	 * Constructor, links an instance to it's implementation.
	 * 
	 * @param impl <code>VehicleImpl</code> that this instance stems from.
	 */
	Vehicle(VehicleImpl impl) {

		super(impl);
	}

	/**
	 * Starts the unit moving from it's current position to the given
	 * co-ordinates.  The act of moving may involve on-the-spot rotation,
	 * 3-point turning, or whatever restrictions the entity may have to overcome
	 * to get from A to B.
	 * 
	 * @param destination Destination co-ordinates to move to.
	 */
//	public abstract void moveTo(Point3i destination);

	/**
	 * @inheritDoc
	 */
	public void delete(GL gl) {

		for (Image[] moveanims: impl.defaultanims) {
			for (Image moveanim: moveanims) {
				moveanim.delete(gl);
			}
		}
		status.delete(gl);
	}

	/**
	 * @inheritDoc
	 */
	public void init(GL gl) {

		for (Image[] moveanims: impl.defaultanims) {
			for (Image moveanim: moveanims) {
				moveanim.init(gl);
			}
		}
		status.init(gl);
	}

	/**
	 * @inheritDoc
	 */
	public void render(GL gl) {

		impl.defaultanims[(int)(impl.defaultanims.length / (360f / heading))][todraw].render(gl);
		if (selected) {
			status.render(gl);
		}
	}
}
