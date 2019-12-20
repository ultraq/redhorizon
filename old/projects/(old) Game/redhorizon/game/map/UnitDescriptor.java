
// =======================================
// Scanner's Java - Common unit descriptor
// =======================================

package redhorizon.game.map;

import redhorizon.game.faction.SubFaction;
import redhorizon.misc.geometry.Point2D;

/**
 * Common class for all initial unit/structure descriptions found in map files.
 * 
 * @author Emanuel Rabina
 */
public abstract class UnitDescriptor {

	// Parent map
	protected final Map map;

	protected SubFaction subfaction;
	protected String unitname;
	protected float hppercentage;
	protected float heading;
	protected Point2D coords;
	protected String triggername;

	/**
	 * Constructor, sets the parent map.
	 * 
	 * @param map The map this structure belongs to.
	 */
	protected UnitDescriptor(Map map) {

		this.map = map;
	}

	/**
	 * Returns the location of the unit/structure.
	 * 
	 * @return Unit's/Structure's location.
	 */
	public Point2D getCoords() {

		return coords;
	}

	/**
	 * Returns the initial heading of the unit/structure.
	 * 
	 * @return Unit's/Structure's heading.
	 */
	public float getHeading() {

		return heading;
	}

	/**
	 * Returns the initial HP of the unit/structure.
	 * 
	 * @return Unit's/Structure's hitpoints.
	 */
	public float getHPPercentage() {

		return hppercentage;
	}

	/**
	 * Returns the faction of the unit/structure.
	 * 
	 * @return Unit's/Structure's faction.
	 */
	public SubFaction getSubFaction() {

		return subfaction;
	}

	/**
	 * Returns the name (ID) of the attached trigger.
	 * 
	 * @return Trigger name.
	 */
	public String getTriggername() {

		return triggername;
	}

	/**
	 * Returns the name (ID) of the unit/structure.
	 * 
	 * @return Unit/Structure name.
	 */
	public String getUnitName() {

		return unitname;
	}
}
