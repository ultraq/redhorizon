
// =================================
// Scanner's Java - C&C vehicle line
// =================================

package redhorizon.game.map;

import redhorizon.game.faction.SubFaction;

/**
 * Abstraction of the C&C vehicle line to contain code common between the RA and
 * TD implementations.
 * 
 * @author Emanuel Rabina
 */
public class VehicleDescriptorCNC extends VehicleDescriptor {

	private static final float MAX_HP      = 256f;
	private static final float MAX_HEADING = 256f;

	/**
	 * Constructor, creates a new initial vehicle description.
	 * 
	 * @param map	The map this vehicle belongs to.
	 * @param specs Line of text from the <code>[UNITS]</code> section of a
	 * 				C&C map file.
	 */
	VehicleDescriptorCNC(Map map, String[] specs) {

		super(map);

		// Split the line into it's underlying specs

		subfaction   = SubFaction.getSubFactionByID(specs[0]);
		unitname     = specs[1];
		hppercentage = Float.parseFloat(specs[2]) / MAX_HP;
		heading      = 360f - ((Float.parseFloat(specs[4]) / MAX_HEADING) * 360f);
		coords       = map.translateMapCoordsXY(Integer.parseInt(specs[3]));
		triggername  = specs[5];

		action = specs[6];
	}
}
