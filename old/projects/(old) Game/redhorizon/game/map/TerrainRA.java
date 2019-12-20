
// ==========================================
// Scanner's Java - RA Terrain implementation
// ==========================================

package redhorizon.game.map;

import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.filemanager.FileManager;
import redhorizon.media.Image;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Point3D;

/**
 * Red Alert implementation of the {@link Terrain} class.  Turns the
 * <code>[Terrain]</code> section of a Red Alert scenario file, into the trees
 * and other terrain used in Red Alert maps.
 * 
 * @author Emanuel Rabina
 */
public class TerrainRA extends Terrain {

	/**
	 * Constructor, parses the <code>[Terrain]</code> section as a series of
	 * lines, each specifying a location and a terrain piece.
	 * 
	 * @param map		  Current Red Alert map.
	 * @param terraindata Section of the map file containing the terrain lines.
	 */
	TerrainRA(MapRA map, java.util.Map<String, String> terraindata) {

		super(map);

		// Get the appropriate terrain image for the given cell co-ordinates
		for (String mapcell: terraindata.keySet()) {
			String terrainname = terraindata.get(mapcell);

			// Locate the right image and co-ordinates
			Image terrainimage = new Image(FileManager.getImagesFile(terrainname + map.theater.ext), 0);
			Point2D centercoord = map.translateXYCoordsPixel(map.translateMapCoordsXY(Integer.parseInt(mapcell)));

			TerrainTile terraintile = new TerrainTile(terrainimage);
			SceneNode tilenode = node.attachChildObject(terraintile);
			tilenode.setPosition(new Point3D(centercoord.getX(), centercoord.getY(), 0));
			tiles.add(terraintile);

			Thread.yield();
		}
	}
}
