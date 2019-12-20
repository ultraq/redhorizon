
// ===============================================
// Scanner's Java - The [Terrain] section of a map
// ===============================================

package redhorizon.game.map;

import redhorizon.engine.scenegraph.AbstractDrawable;
import redhorizon.engine.scenegraph.AbstractSceneObject;
import redhorizon.media.Image;
import redhorizon.misc.geometry.Point3D;
import redhorizon.misc.geometry.Rectangle2D;
import static redhorizon.game.GameObjectDepths.DEPTH_MAP_TERRAIN;

import java.util.ArrayList;

import javax.media.opengl.GL;

/**
 * Representation of a map's <code>[Terrain]</code> section.  This section
 * contains co-ordinates for the map's trees and other unpassable bits.
 * 
 * @author Emanuel Rabina
 */
public abstract class Terrain extends AbstractSceneObject {

	// The parent map
	protected final Map map;

	// List of terrain elements
	protected final ArrayList<TerrainTile> tiles = new ArrayList<TerrainTile>();

	/**
	 * Constructor, assigns the current map to this <code>Terrain</code>.
	 * 
	 * @param map <code>Map</code> this <code>Terrain</code> belongs to.
	 */
	protected Terrain(Map map) {

		map.getSceneNode().attachChildObject(this);
		setPosition(new Point3D(0, 0, DEPTH_MAP_TERRAIN));
		this.map = map;
	}

	/**
	 * Makes the terrain invisible.
	 */
	protected void hide() {

		for (TerrainTile tile: tiles) {
			tile.erase();
		}
	}

	/**
	 * Makes the terrain visible.
	 */
	protected void show() {

		for (TerrainTile tile: tiles) {
			tile.draw();
		}
	}

	/**
	 * Representation of a single terrain part, normally trees, which can be
	 * found throughout a map.
	 */
	protected class TerrainTile extends AbstractDrawable {

		protected final Image terrainimage;

		/**
		 * Constructor, uses the given image for the terrain piece.
		 * 
		 * @param image Image to use for this terrain.
		 */
		protected TerrainTile(Image image) {

			terrainimage = image;
		}

		/**
		 * @inheritDoc
		 */
		protected Rectangle2D getBoundingArea() {

			return terrainimage.getBoundingArea();
		}

		/**
		 * @inheritDoc
		 */
		public void delete(GL gl) {

			terrainimage.delete(gl);
		}

		/**
		 * @inheritDoc
		 */
		public void init(GL gl) {

			terrainimage.init(gl);
		}

		/**
		 * @inheritDoc
		 */
		public void render(GL gl) {

			terrainimage.render(gl);
		}
	}
}
