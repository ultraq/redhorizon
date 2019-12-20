
// =========================
// Scanner's Java - Map area
// =========================

package redhorizon.game.map;

import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.ini.IniFile;
import redhorizon.game.objects.units.Structure;
import redhorizon.game.objects.units.UnitFactory;
import redhorizon.game.objects.units.Vehicle;
import redhorizon.media.ImageTiled;
import redhorizon.media.MediaManager;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Point3D;
import redhorizon.misc.geometry.Rectangle2D;
import redhorizon.utilities.ImageUtility;

/**
 * Red Alert implementation of a {@link Map}.
 * 
 * @author Emanuel Rabina
 */
public class MapRA extends MapCNC {

	// List of RA-specific map parameters
	private static final String SECTION_MAPPACK = "MapPack";
	private static final int    MAX_WIDTH       = 128;
	private static final int    MAX_HEIGHT      = 128;
	private static final int    CENTER_WAYPOINT = 98;

	/**
	 * Constructor, loads map's attributes from the settings file.
	 * 
	 * @param mapfile C&C map file.
	 */
	MapRA(IniFile mapfile) {

		super(mapfile);

		// Set the background tile to act as a theater underlay
		ImagesFile mapbg = FileManager.getImagesFile("Clear1" + theater.ext);
		background = new ImageTiled(mapbg + "_Combined", mapbg.format(),
				mapbg.width() * 5, mapbg.height() * 4,
				ImageUtility.combineImages(5, 4, mapbg, MediaManager.getPalette()),
				true, MAX_WIDTH / 5, MAX_HEIGHT / 4);
		node.attachChildObject(background);

		// Create RA-specific map sections
		mappack     = new MapPackRA(this, mapfile.getSection(SECTION_MAPPACK));
		overlaypack = new OverlayPackRA(this, mapfile.getSection(SECTION_OVERLAYPACK));
		terrain     = new TerrainRA(this, mapfile.getSection(SECTION_TERRAIN));

		UnitFactory unitfactory = UnitFactory.currentUnitFactory();

		// Create initial structures
		java.util.Map<String,String> structuresraw = mapfile.getSection(SECTION_STRUCTURES);
		for (String structureline: structuresraw.values()) {
			String[] specs = structureline.split(",");
			StructureDescriptorRA structuredesc = new StructureDescriptorRA(this, specs);
			Structure structure = unitfactory.createStructure(structuredesc);

			// Adjust structure location, add to map
			Rectangle2D area = structure.getBoundingArea();
			Point2D location = translateXYCoordsPixel(structuredesc.getCoords());
			SceneNode strucnode = structurenode.attachChildObject(structure);
			strucnode.setPosition(new Point3D(location.add(0, -area.height() + CELL_WIDTH)));

			structures.add(structure);
			Thread.yield();
		}

		// Create initial vehicles
		java.util.Map<String,String> vehiclesraw = mapfile.getSection(SECTION_VEHICLES);
		for (String vehicleline: vehiclesraw.values()) {
			String[] specs = vehicleline.split(",");
			VehicleDescriptorCNC vehicledesc = new VehicleDescriptorCNC(this, specs);
			Vehicle vehicle = unitfactory.createVehicle(vehicledesc);

			// Add unit to map
			Point2D location = translateXYCoordsPixel(vehicledesc.getCoords());
			SceneNode vehnode = vehiclenode.attachChildObject(vehicle);
			vehnode.setPosition(new Point3D(location));

			vehicles.add(vehicle);
			Thread.yield();
		}
	}

	/**
	 * @inheritDoc
	 */
	public Point2D getCameraInitCoords() {

		return translateXYCoordsPixel(getWaypoint(CENTER_WAYPOINT));
	}

	/**
	 * @inheritDoc
	 */
	public Point2D translateMapCoordsXY(int cell) {

		return translateMapCoordsXY(cell % MAX_HEIGHT, cell / MAX_WIDTH);
	}

	/**
	 * @inheritDoc
	 */
	public Point2D translateMapCoordsXY(int xcoord, int ycoord) {

		return new Point2D(xcoord - (MAX_WIDTH >> 1), (MAX_HEIGHT >> 1) - ycoord);
	}
}
