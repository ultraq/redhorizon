
// ===================================================
// Scanner's Java - The [OverlayPack] section of a map
// ===================================================

package redhorizon.game.map;

import redhorizon.engine.scenegraph.AbstractDrawable;
import redhorizon.engine.scenegraph.AbstractSceneObject;
import redhorizon.media.Image;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Point3D;
import redhorizon.misc.geometry.Rectangle2D;
import static redhorizon.game.GameObjectDepths.DEPTH_MAP_OVERLAYPACK;

import javax.media.opengl.GL;

import java.util.LinkedHashMap;

/**
 * Representation of a map's <code>[OverlayPack]</code> section.  Like
 * {@link MapPack}, it is a representation of a list of tiles defining the
 * overlay-level objects throughout the map.
 * 
 * @author Emanuel Rabina
 */
public abstract class OverlayPack extends AbstractSceneObject {

	// The parent map
	protected final Map map;

	// Array of overlay areas
	protected final LinkedHashMap<Point2D,OverlayTile> tiles = new LinkedHashMap<Point2D,OverlayTile>();

	/**
	 * Constructor, assigns the current map to this <code>OverlayPack</code>.
	 * 
	 * @param map <code>Map</code> this <code>OverlayPack</code> belongs to.
	 */
	protected OverlayPack(Map map) {

		map.getSceneNode().attachChildObject(this);
		setPosition(new Point3D(0, 0, DEPTH_MAP_OVERLAYPACK));
		this.map = map;
	}

	/**
	 * Makes the overlaypack invisible.
	 */
	protected void hide() {

		// Set each overlay piece to erase
		for (OverlayTile tile: tiles.values()) {
			tile.erase();
		}
	}

	/**
	 * Makes the overlaypack visible.
	 */
	protected void show() {

		for (OverlayTile tile: tiles.values()) {
			tile.draw();
		}
	}

	/**
	 * Representation of an Overlay section tile.  Parts of this representation
	 * include the image to use when drawing it, and the 'intensity' of the tile
	 * (which for a piece of tiberium is something like how much is there, or
	 * for a wall it is how many hitpoints it has). 
	 */
	protected abstract class OverlayTile extends AbstractDrawable {

		protected final int x;
		protected final int y;
		protected final String imagename;
		protected Image overlayimage;

		/**
		 * Constructor, sets the type of tile it is.
		 * 
		 * @param x	   X co-ordinate of this tile.
		 * @param y	   Y co-ordinate of this tile.
		 * @param name Name for this tile, usually the image associated with it.
		 */
		protected OverlayTile(int x, int y, String name) {

			this.x         = x;
			this.y         = y;
			this.imagename = name;
		}

		/**
		 * @inheritDoc
		 */
		public void delete(GL gl) {

			overlayimage.delete(gl);
		}

		/**
		 * @inheritDoc
		 */
		protected Rectangle2D getBoundingArea() {

			return overlayimage.getBoundingArea();
		}

		/**
		 * @inheritDoc
		 */
		public void init(GL gl) {

			overlayimage.init(gl);
		}

		/**
		 * Because overlay objects rely on neighbouring overlay objects to
		 * determine which image to use, this method gives them that opportunity
		 * to load the appropriate image during the load phase of the map, and
		 * before the init phase of the graphics engine.
		 */
		protected abstract void initImage();

		/**
		 * @inheritDoc
		 */
		public void render(GL gl) {

			overlayimage.render(gl);
		}
	}
}
