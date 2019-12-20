
// =================================================
// Scanner's Java - Red Alert -specific unit factory
// =================================================

package redhorizon.game.objects.units;

import redhorizon.game.faction.SubFaction;
import redhorizon.game.map.StructureDescriptor;
import redhorizon.game.map.VehicleDescriptor;
import redhorizon.media.MediaManager;

/**
 * Red Alert -specific implementation of the {@link UnitFactory}.  Contains
 * methods made for parsing RA requirements and creating the appropriate unit.
 * 
 * @author Emanuel Rabina
 */
public class UnitFactoryRA extends UnitFactory {

	private SubFaction lastsubfaction;

	/**
	 * Default constructor, does nothing special.
	 */
	UnitFactoryRA() {
	}

	/**
	 * Updates the current colour palette as necessary.
	 * 
	 * @param subfaction Subfaction for which the unit is being built.
	 */
	private synchronized void checkPalette(SubFaction subfaction) {

		if (lastsubfaction != subfaction) {
			lastsubfaction = subfaction;
			MediaManager.getPalette().applyShift(subfaction.getColour());
		}
	}

	/**
	 * @inheritDoc
	 */
	public Structure createStructure(StructureDescriptor descriptor) {

		// Set the palette to the current faction
		SubFaction subfaction = descriptor.getSubFaction();
		checkPalette(subfaction);

		// Create the structure, apply descriptor modifications
		Structure structure = structurefactory.createInstance(subfaction, descriptor.getUnitName());
		structure.setHPPercentage(descriptor.getHPPercentage());

		return structure;
	}

	/**
	 * @inheritDoc
	 */
	public Vehicle createVehicle(VehicleDescriptor descriptor) {

		// Set the palette to the current faction
		SubFaction subfaction = descriptor.getSubFaction();
		checkPalette(subfaction);

		// Create and set up a new vehicle from the specs
		Vehicle vehicle = vehiclefactory.createInstance(subfaction, descriptor.getUnitName());
		vehicle.setHPPercentage(descriptor.getHPPercentage());
		vehicle.setHeading(descriptor.getHeading());

		return vehicle;
	}
}
