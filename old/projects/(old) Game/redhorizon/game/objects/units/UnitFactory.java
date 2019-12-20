
// ======================================
// Scanner's Java - Unit creation factory
// ======================================

package redhorizon.game.objects.units;

import redhorizon.game.Rules;
import redhorizon.game.map.StructureDescriptor;
import redhorizon.game.map.VehicleDescriptor;
import redhorizon.game.objects.ObjectFactory;
import redhorizon.misc.CNCGameTypes;
import redhorizon.xml.units.XMLStructure;
import redhorizon.xml.units.XMLUnit;
import redhorizon.xml.units.XMLVehicle;

/**
 * Factory class for creating the units used throughout the game.
 * 
 * @author Emanuel Rabina
 */
public abstract class UnitFactory {

	// Singleton factory instance
	private static UnitFactory factory;

	// Sub-factory instances
	protected UnitFactoryXMLPart<VehicleImpl,Vehicle,XMLVehicle> vehiclefactory =
		new UnitFactoryXMLPart<VehicleImpl,Vehicle,XMLVehicle>(VehicleImpl.class, XMLVehicle.class);
	protected UnitFactoryXMLPart<StructureImpl,Structure,XMLStructure> structurefactory =
		new UnitFactoryXMLPart<StructureImpl,Structure,XMLStructure>(StructureImpl.class, XMLStructure.class);

	/**
	 * Default constructor, for subclasses.
	 */
	protected UnitFactory() {
	}

	/**
	 * Creates and returns a game-specific instance of this unit factory.
	 * 
	 * @return A <code>UnitFactory</code> specific to the game type.
	 */
	public static UnitFactory createUnitFactory() {

		switch (CNCGameTypes.getCurrentType()) {
		case RED_ALERT:
			factory = new UnitFactoryRA();
			break;
		case TIBERIUM_DAWN:
//			factory = new UnitFactoryTD();
		}
		return factory;
	}

	/**
	 * Returns the current instance of the unit factory.
	 * 
	 * @return The game-specific unit factory, or <code>null</code> if none
	 * 		   exists.
	 */
	public static UnitFactory currentUnitFactory() {

		return factory;
	}

	/**
	 * Creates a single <code>Structure</code> from the structure descrption
	 * given by the current map.
	 * 
	 * @param descriptor Structure description.
	 * @return New structure from the given spec.
	 */
	public abstract Structure createStructure(StructureDescriptor descriptor);

	/**
	 * Creates a single <code>Vehicle</code> for a vehicle description given by
	 * the current map.
	 * 
	 * @param descriptor Vehicle description.
	 * @return New vehicle from the given spec.
	 */
	public abstract Vehicle createVehicle(VehicleDescriptor descriptor);

	/**
	 * Class which implements just the XML retrieval method, which this unit
	 * factory requires.
	 * 
	 * @param <M> Implementatiion type.
	 * @param <N> Instance type.
	 * @param <X> XML binding type.
	 */
	protected class UnitFactoryXMLPart<M extends UnitImpl<N>, N extends Unit<M>, X extends XMLUnit>
		extends ObjectFactory<M, N, X> {

		/**
		 * Constructor, initializes this factory for unit creation.
		 * 
		 * @param implclass The class used to create new unit implementations.
		 * @param xmlimpl	The Java-from-XML class which represents units.
		 */
		private UnitFactoryXMLPart(Class<? extends M> implclass, Class<? extends X> xmlimpl) {

			super(implclass, xmlimpl);
		}

		/**
		 * @inheritDoc
		 */
		@SuppressWarnings("unchecked")
		protected X retrieveXMLData(String id) {

			return (X)Rules.currentRules().getUnitByID(id);
		}
	}
}
