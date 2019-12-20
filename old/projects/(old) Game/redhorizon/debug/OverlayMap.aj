
// =========================================================
// Scanner's AspectJ - Creates RAED-like overlays on the map
// =========================================================

package redhorizon.debug;

import redhorizon.engine.scenegraph.AbstractDrawable;
import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.game.map.Map;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Point3D;
import redhorizon.misc.geometry.Rectangle2D;

import static redhorizon.game.GameObjectDepths.DEPTH_OVERLAY_MAP;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

/**
 * Creates RAED-like (C&C Red Alert Scenario Editor) overlays over the map.
 * These are the things like boundary lines, waypoint squares, celltrigger
 * names, or anything else that can be identified by a visible representation.
 * 
 * @author Emanuel Rabina
 */
public aspect OverlayMap {

	// Overlay parts
	private static SceneNode overlaynode;
	private static MapAxis axis;
	private static MapBoundary boundary;

	/**
	 * Gets the map attributes after a map has been created.
	 * 
	 * @param map The recently created map.
	 */
	after() returning(Map map):
		execution(public static Map Map.createMap(..)) {

		// Create an overlay node, or retain the current one
		if (overlaynode == null) {
			overlaynode = map.getSceneNode().createChildSceneNode();
		}
		overlaynode.setPosition(new Point3D(0, 0, DEPTH_OVERLAY_MAP));

		// Create the overlay parts and attach to the overlay node
		axis = new MapAxis();
		overlaynode.attachChildObject(axis);
		boundary = new MapBoundary(map);
		overlaynode.attachChildObject(boundary);
	}

	/**
	 * Shows all overlay elements when the map is shown.
	 */
	after() returning:
		execution(public void Map.show()) {

		axis.draw();
		boundary.draw();
	}

	/**
	 * Hides all overlay elements when the map is hidden.
	 */
	after() returning:
		execution(public void Map.hide()) {

		axis.erase();
		boundary.erase();
	}

	/**
	 * Abstract overlay class, takes-care of the common methods.
	 */
	private static abstract class AbstractOverlay extends AbstractDrawable {

		/**
		 * @inheritDoc
		 */
		public void delete(GL gl) {
		}

		/**
		 * @inheritDoc
		 */
		public void init(GL gl) {
		}

		/**
		 * Renders the overlay, pushing/popping the texture environment mode
		 * before/after rendering.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		public void render(GL gl) {

			// Store the texture mode
			int[] texenv = new int[1];
			gl.glGetTexEnviv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv, 0);

			// Draw overlay using overlay texture mode
			gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
			renderOverlay(gl);

			// Restore the previous texture mode
			gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv[0]);
		}

		/**
		 * Draws the overlay.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		abstract void renderOverlay(GL gl);
	}

	/**
	 * Map overlay class to draw the XY axis markers.
	 */
	private static class MapAxis extends AbstractOverlay {

		/**
		 * Default constructor.
		 */
		private MapAxis() {
		}

		/**
		 * @inheritDoc
		 */
		protected Rectangle2D getBoundingArea() {

			return new Rectangle2D(-2400, -2400, 2400, 2400);
		}

		/**
		 * Draws the XY axis over the map.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		void renderOverlay(GL gl) {

			// Draw XYZ lines
			gl.glColor4f(1, 0, 0, 1);
			gl.glBegin(GL_LINES);
			{
				gl.glVertex2i(-2400, 0);	gl.glVertex2i(2400, 0);
				gl.glVertex2i(0, 2400);		gl.glVertex2i(0, -2400);
			}
			gl.glEnd();

			// Draw markers along XY axes every 24px
			for (int i = -2400; i <= 2400; i += 24) {
				gl.glBegin(GL_LINES);
				{
					gl.glVertex2i(i, 5);	gl.glVertex2i(i, -5);
					gl.glVertex2i(5, i);	gl.glVertex2i(-5, i);
				}
				gl.glEnd();
			}
		}
	}

	/**
	 * Map overlay class to draw the map boundary lines.
	 */
	private static class MapBoundary extends AbstractOverlay {

		private final Rectangle2D bounds;
		private final int coordx;
		private final int coordy;
		private final int coordw;
		private final int coordh;

		/**
		 * Constructor, attaches this boundary to the map.
		 * 
		 * @param map The map to surround.
		 */
		private MapBoundary(Map map) {

			Rectangle2D mapbounds = map.getBounds();
			Point2D boundsxy = map.translateXYCoordsPixel(mapbounds.center());
			Point2D boundswh = map.translateXYCoordsPixel(mapbounds.width(), mapbounds.height());

			coordx = boundsxy.getX();
			coordy = boundsxy.getY();
			coordw = coordx + boundswh.getX();
			coordh = coordy + boundswh.getY();

			bounds = new Rectangle2D(coordx, coordy, coordw, coordh);
		}

		/**
		 * @inheritDoc
		 */
		protected Rectangle2D getBoundingArea() {

			return bounds;
		}

		/**
		 * Draws the map overlays onto the map.
		 * 
		 * @param gl Current OpenGL pipeline.
		 */
		void renderOverlay(GL gl) {

			// Draw the outline
			gl.glColor4f(1f, 1f, 0f, 1f);
			gl.glBegin(GL_LINE_LOOP);
			{
				gl.glVertex2i(coordx, coordy);
				gl.glVertex2i(coordx, coordh);
				gl.glVertex2i(coordw, coordh);
				gl.glVertex2i(coordw, coordy);
			}
			gl.glEnd();
		}
	}
}
