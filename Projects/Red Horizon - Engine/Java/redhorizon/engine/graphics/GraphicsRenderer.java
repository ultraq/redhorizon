
package redhorizon.engine.graphics;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

/**
 * OpenGL renderer for the graphics subsystem.
 * 
 * @author Emanuel Rabina
 */
public class GraphicsRenderer {

	private GL gl;

	/**
	 * Render the viewport based off the given camera settings.
	 * 
	 * @param camera
	 */
	public void renderViewport(Camera camera) {

		// Set up the viewport
		gl.glViewport(0, 0, viewport.getWidth(), viewport.getHeight());
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(viewvolume.getLeft(), viewvolume.getRight(),
				   viewvolume.getBottom(), viewvolume.getTop(),
				   viewvolume.getFront(), viewvolume.getBack());
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
	}
}
