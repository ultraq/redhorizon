
// =========================
// Scanner's Java - Map area
// =========================

package redhorizon.game.map;

import redhorizon.engine.scenegraph.AbstractSceneObject;
import redhorizon.engine.scenegraph.SceneManager;
import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.MapFile;
import redhorizon.filetypes.ini.IniFile;
import redhorizon.game.mission.Mission;
import redhorizon.game.objects.units.Structure;
import redhorizon.game.objects.units.Vehicle;
import redhorizon.media.ImageTiled;
import redhorizon.misc.CNCGameTypes;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Point3D;
import redhorizon.misc.geometry.Rectangle2D;
import static redhorizon.game.GameObjectDepths.DEPTH_MAP;
import static redhorizon.game.GameObjectDepths.DEPTH_STRUCTURE;
import static redhorizon.game.GameObjectDepths.DEPTH_VEHICLE;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class acts as the map on which a mission may take place.  From a map
 * file (INI, MPR, BIN), it parses the sections of the file, each of which can
 * then be requested using this class' accessor methods.  A Map contains just
 * the basics required to host a map.  Extensions of the map file, such as
 * teamtypes and triggers, are specific to the {@link Mission} class.<br>
 * <br>
 * The underlying map will be a 2D grid/array representation of the tiles of the
 * map.  Each tile can be a part of several contexts, whether it be a plain land
 * tile, rough, road, etc.<br>
 * <br>
 * The map co-ordinates used by Red Horizon are different from that of TD or RA,
 * where instead of the top-left corner being (0,0), it's instead
 * (-map_width / 2, map_height / 2), ie: the same as the XY world co-ordinates
 * of the audio and graphics engines, where the center of the potential map area
 * is at (0,0).
 * 
 * @author Emanuel Rabina
 */
public abstract class Map extends AbstractSceneObject {

	// Singleton map instance
	private static Map map;

	// Map attributes
	protected String name;
	protected Theaters theater;
	protected Rectangle2D bounds;

	// Map parts
	protected HashMap<Integer,Point2D> waypoints = new HashMap<Integer,Point2D>();
	protected ImageTiled background;
	protected MapPack mappack;
	protected OverlayPack overlaypack;
	protected Terrain terrain;

	protected ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	protected ArrayList<Structure> structures = new ArrayList<Structure>();

	protected final SceneNode vehiclenode;
	protected final SceneNode structurenode;

	/**
	 * Constructor, initializes several common parts of a map which subclasses
	 * should fill-in in their constructors.
	 * 
	 * @param mapfile File containing the map data.
	 */
	@SuppressWarnings("unused")
	protected Map(MapFile mapfile) {

		// Create map components node
		SceneNode rootnode = SceneManager.currentSceneManager().getRootSceneNode();
		rootnode.attachChildObject(this);
		setPosition(new Point3D(0, 0, DEPTH_MAP));

		// All other nodes branch from the unit node
		vehiclenode = node.createChildSceneNode();
		vehiclenode.setPosition(new Point3D(0, 0, DEPTH_VEHICLE));
		structurenode = node.createChildSceneNode();
		structurenode.setPosition(new Point3D(0, 0, DEPTH_STRUCTURE));
	}

	/**
	 * Constructs and returns a new map based on the given map file.
	 * 
	 * @param mapdesc Descriptor of the map to load.
	 * @return New <code>Map</code>, or null if the map file could not be
	 * 		   understood.
	 */
	public static Map createMap(MapDescriptor mapdesc) {

		// Load the map
		switch (CNCGameTypes.getCurrentType()) {
		case TIBERIUM_DAWN:
//			map = new MapTD(mapdesc);
			break;
		case RED_ALERT:
			map = new MapRA((IniFile)FileManager.getMapFile(mapdesc.getFile()));
			break;
		}

		return map;
	}

	/**
	 * Returns the currently running instance of a <code>Map</code>.
	 * 
	 * @return The current <code>Map</code>.
	 */
	public static Map currentMap() {

		return map;
	}

	/**
	 * Returns the bounds of the map.  The player's actions are restricted to
	 * the area defined by the bounds.
	 * 
	 * @return The player boundaries of the map.
	 */
	public Rectangle2D getBounds() {

		return bounds;
	}

	/**
	 * Returns the starting position of where the player view should be centered
	 * at the beginning of loading a map.
	 * 
	 * @return XY world co-ordinates of where the player's view starts.
	 */
	public abstract Point2D getCameraInitCoords();

	/**
	 * Returns the name of the map.
	 * 
	 * @return Map name.
	 */
	public String getName() {

		return name;
	}

	/**
	 * Returns the current tileset used by the map.
	 * 
	 * @return Map 'theater' (tileset).
	 */
	public Theaters getTheater() {

		return theater;
	}

	/**
	 * Returns the co-ordinate value of the specified waypoint.
	 * 
	 * @param waypoint Number of the waypoint.
	 * @return The XY map co-ordinates of the specified waypoint.
	 */
	public Point2D getWaypoint(int waypoint) {

		return waypoints.get(waypoint);
	}

	/**
	 * Makes the map invisible.
	 */
	public void hide() {

		background.erase();
		mappack.hide();
		terrain.hide();
		overlaypack.hide();

		for (Vehicle vehicle: vehicles) {
			vehicle.erase();
		}
		for (Structure structure: structures) {
			structure.erase();
		}
	}

	/**
	 * Sets all of the map components to be visisble.
	 */
	public void show() {

		background.draw();
		mappack.show();
		terrain.show();
		overlaypack.show();

		for (Vehicle vehicle: vehicles) {
			vehicle.draw();
		}
		for (Structure structure: structures) {
			structure.draw();
		}
	}

	/**
	 * Converts a TD or RA map co-ordinate to the XY map co-ordinate system used
	 * by Red Horizon.  The XY co-ordinate system is the same as used by the
	 * world co-ordinates of the audio and graphics engines.
	 * 
	 * @param cell The original co-ordinate value in map cells.
	 * @return A <code>Point2i</code> of the new X and Y.
	 */
	public abstract Point2D translateMapCoordsXY(int cell);

	/**
	 * Converts a TD or RA map co-ordinate to the XY map co-ordinate system used
	 * by Red Horizon.  The XY co-ordinate system is the same as used by the
	 * world co-ordinates of the audio and graphics engines.
	 * 
	 * @param xcoord X co-ordinate.
	 * @param ycoord Y co-ordinate.
	 * @return A <code>Point2i</code> of the new X and Y.
	 */
	public abstract Point2D translateMapCoordsXY(int xcoord, int ycoord);

	/**
	 * When passed an XY co-ordinate, returns a corresponding pixel-precise
	 * world co-ordinate, used mainly for audio and graphics rendering purposes.
	 * The value will point to the center of a map cell.
	 * 
	 * @param coords <code>Point2i</code> of XY co-ordinates.
	 * @return A <code>Point2i</code> depicting the world co-ordinate at the
	 * 		   center of the given X & Y map cell co-ordinates.
	 */
	public abstract Point2D translateXYCoordsPixel(Point2D coords);

	/**
	 * When passed an XY co-ordinate, returns a corresponding pixel-precise
	 * world co-ordinate, used mainly for audio and graphics rendering purposes.
	 * The value will point to the center of a map cell.
	 * 
	 * @param cellx	 X co-ordinate in map cells.
	 * @param celly	 Y co-ordinate in map cells.
	 * @return A <code>Point2i</code> depicting the world co-ordinate at the
	 * 		   center of the given X & Y map cell co-ordinates.
	 */
	public abstract Point2D translateXYCoordsPixel(int cellx, int celly);
}
