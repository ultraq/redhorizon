
// ===============================
// Scanner's Java - Common C&C map
// ===============================

package redhorizon.game.map;

import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.PaletteFile;
import redhorizon.filetypes.ini.IniFile;
import redhorizon.media.MediaManager;
import redhorizon.misc.CNCAlphaMask;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Rectangle2D;

/**
 * Abstraction of the C&C map type to include components common to both RA and
 * TD.
 * 
 * @author Emanuel Rabina
 */
public abstract class MapCNC extends Map {

	// C&C map constants
	static final int CELL_WIDTH = 24;
	static final int CELL_HEIGHT = 24;

	// List of sections common to most maps
	static final String SECTION_BASIC       = "Basic";
	static final String SECTION_MAP         = "Map";
	static final String SECTION_WAYPOINTS   = "Waypoints";
	static final String SECTION_TERRAIN     = "TERRAIN";
	static final String SECTION_SMUDGE      = "SMUDGE";
	static final String SECTION_OVERLAYPACK = "OverlayPack";

	static final String SECTION_INFANTRY     = "INFANTRY";
	static final String SECTION_VEHICLES     = "UNITS";
	static final String SECTION_SHIPS        = "SHIPS";
	static final String SECTION_STRUCTURES   = "STRUCTURES";

	// List of parameters common to most maps
	static final String PARAM_NAME    = "Name";
	static final String PARAM_THEATER = "Theater";
	static final String PARAM_X       = "X";
	static final String PARAM_Y       = "Y";
	static final String PARAM_WIDTH   = "Width";
	static final String PARAM_HEIGHT  = "Height";

	/**
	 * Constructor, takes the given map name and loads the map section.  For RA,
	 * that is the INI file, for TD, the INI and BIN file.
	 * 
	 * @param mapfile C&C map file.
	 */
	MapCNC(IniFile mapfile) {

		super(mapfile);

		// Load parts common across all C&C map types

		// Load map name, size, and tileset
		name    = mapfile.getValue(SECTION_BASIC, PARAM_NAME);
		theater = Theaters.getMatchingType(mapfile.getValue(SECTION_MAP, PARAM_THEATER));

		int mapx = Integer.parseInt(mapfile.getValue(SECTION_MAP, PARAM_X));
		int mapy = Integer.parseInt(mapfile.getValue(SECTION_MAP, PARAM_Y));
		int mapw = Integer.parseInt(mapfile.getValue(SECTION_MAP, PARAM_WIDTH));
		int maph = Integer.parseInt(mapfile.getValue(SECTION_MAP, PARAM_HEIGHT));

		bounds = new Rectangle2D(translateMapCoordsXY(mapx, mapy),
				translateMapCoordsXY(mapx + mapw, mapy + maph));

		// Set the image palette to match the theater and use C&C transparencies
		PaletteFile palette = FileManager.getPaletteFile(
				MediaManager.Palettes.getMatchingType(theater.label).filename);
		palette.applyAlpha(CNCAlphaMask.FULL);
		MediaManager.setPalette(palette);

		// Create waypoints
		java.util.Map<String,String> secwaypoints = mapfile.getSection(SECTION_WAYPOINTS);
		for (java.util.Map.Entry<String,String> param: secwaypoints.entrySet()) {
			int coords = Integer.parseInt(param.getValue());
			waypoints.put(new Integer(param.getKey()), translateMapCoordsXY(coords));
			Thread.yield();
		}
	}

	/**
	 * @inheritDoc
	 */
	public Point2D translateXYCoordsPixel(Point2D coords) {

		return translateXYCoordsPixel(coords.getX(), coords.getY());
	}

	/**
	 * @inheritDoc
	 */
	public Point2D translateXYCoordsPixel(int cellx, int celly) {

		return new Point2D(cellx * CELL_WIDTH, celly * CELL_HEIGHT);
	}
}
