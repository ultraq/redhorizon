
// ===============================================
// Scanner's Java - The [MapPack] section of a map
// ===============================================

package redhorizon.game.map;

import redhorizon.engine.scenegraph.AbstractSceneObject;
import redhorizon.media.Image;
import redhorizon.misc.geometry.Point3D;

import static redhorizon.game.GameObjectDepths.DEPTH_MAP_MAPPACK;

import java.util.ArrayList;

/**
 * Representation of the map's <code>[MapPack]</code> section (in TD, this
 * section is in a different file, the BIN file).  Essentially a list of tiles
 * which cover the entire map, each with their appropriate grid co-ordinate in a
 * map.
 * 
 * @author Emanuel Rabina
 */
public abstract class MapPack extends AbstractSceneObject {

	// The parent map
	protected final Map map;

	// Tiles which consist of the mappack images
	protected final ArrayList<Image> tiles = new ArrayList<Image>();

	/**
	 * Constructor, assigns the current map to the <code>MapPack</code>.
	 * 
	 * @param map <code>Map</code> this <code>MapPack</code> belongs to.
	 */
	protected MapPack(Map map) {

		map.getSceneNode().attachChildObject(this);
		setPosition(new Point3D(0, 0, DEPTH_MAP_MAPPACK));
		this.map = map;
	}

	/**
	 * Makes the mappack invisible.
	 */
	protected void hide() {

		// Set all of the mappack tiles to erase
		for (Image tile: tiles) {
			tile.erase();
		}
	}

	/**
	 * Makes the mappack visible.
	 */
	protected void show() {

		// Set all of the mappack tiles to draw
		for (Image tile: tiles) {
			tile.draw();
		}
	}
}
