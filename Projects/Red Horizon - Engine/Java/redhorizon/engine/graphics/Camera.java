
package redhorizon.engine.graphics;

import redhorizon.geometry.Cube;
import redhorizon.scenegraph.AbstractTransformable;

/**
 * Basic orthographic-projection camera, used for sprite-based / 2D programs.
 * 
 * @author Emanuel Rabina
 */
public class Camera {

	/**
	 * Camera projection types.
	 */
	public static enum ProjectionTypes {

		ORTHOGRAPHIC,
		PERSPECTIVE;
	}

	// Camera defaults
//	private static final int CAMERA_PROJECTION_WIDTH  = 800;
//	private static final int CAMERA_PROJECTION_HEIGHT = 600;
	private static final int CAMERA_PROJECTION_DEPTH  = 100;

	/**
	 * Constructor, sets-up an orthographic-projection camera with the specified
	 * viewing area.  The viewing depth defaults to 100.
	 * 
	 * @param width	 Viewing width of the camera.
	 * @param height Viewing height of the camera.
	 */
	Camera(int width, int height) {

		int depth  = CAMERA_PROJECTION_DEPTH;

	}

	/**
	 * Returns the current viewing projection (the projection volume and the
	 * current central position).
	 * 
	 * @return 3D section of the game world the camera is currently focused on.
	 */
	public Rectangle3D getCurrentProjection() {

		return viewvolume.offset(getPosition());
	}

	/**
	 * Returns the type of projection (perspective or orthographic) used by this
	 * camera.
	 * 
	 * NOTE: Currently only supports orthographic projection.
	 * 
	 * @return One of the types in the {@link ProjectionTypes} enum.
	 */
	public ProjectionTypes getProjectionType() {

		return ProjectionTypes.ORTHOGRAPHIC;
	}

	/**
	 * Returns the projection volume of the camera.
	 * 
	 * @return The volume of space taken-up by the camera projection.
	 */
	public Rectangle3D getProjectionVolume() {

		return viewvolume;
	}

	/**
	 * @inheritDoc
	 */
	public void rendering(GL gl) {

		// Update the position of the camera
		Point3D diff = getPosition().difference(lastpos);
		if (!diff.equals(Point3D.DEFAULT)) {
			gl.glTranslatef(-diff.getX(), -diff.getY(), -diff.getZ());
			lastpos = lastpos.add(diff);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void shutdown(GL gl) {

	}
}
