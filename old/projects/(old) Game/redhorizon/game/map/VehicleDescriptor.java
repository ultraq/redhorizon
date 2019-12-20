
// =============================================
// Scanner's Java - Starting vehicle description
// =============================================

package redhorizon.game.map;

/**
 * Class to describe a vehicle which comes with the map.
 * 
 * @author Emanuel Rabina
 */
public abstract class VehicleDescriptor extends UnitDescriptor {

	protected String action;

	/**
	 * Constructor, creates a new initial vehicle description.
	 * 
	 * @param map The map this vehicle belongs to.
	 */
	protected VehicleDescriptor(Map map) {

		super(map);
	}
}
