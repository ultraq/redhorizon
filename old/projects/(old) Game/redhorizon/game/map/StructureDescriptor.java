
// ===============================================
// Scanner's Java - Starting structure description
// ===============================================

package redhorizon.game.map;

/**
 * Class to describe a structure which comes with the map.
 * 
 * @author Emanuel Rabina
 */
public abstract class StructureDescriptor extends UnitDescriptor {

	/**
	 * Constructor, creates a new initial structure description.
	 * 
	 * @param map The map this structure belongs to.
	 */
	protected StructureDescriptor(Map map) {

		super(map);
	}
}
