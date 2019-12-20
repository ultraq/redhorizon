
// =========================================================
// Scanner's AspectJ - Draws a box around an object's bounds
// =========================================================

package redhorizon.debug;

import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.misc.geometry.Rectangle2D;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

/**
 * Draws a box around each scene object, depicting their drawable bounds.  Also
 * used to ensure that the scenegraph's bounding volumes are working correctly.
 * 
 * @author Emanuel Rabina
 */
public privileged aspect OverlayBounds {

	/**
	 * Draws the bounding box of each node in the scene.
	 * 
	 * @param node Currently drawing <code>SceneNode</code>.
	 * @param gl   Current OpenGL pipeline.
	 */
	before(SceneNode node, GL gl):
		call(public void GL.glPopMatrix()) && withincode(void SceneNode.renderNode(GL)) &&
		this(node) && target(gl) {

		Rectangle2D area = node.boundingarea;

		// Skip 'empty' nodes
		if (area.width() == 0 && area.height() == 0) {
			return;
		}

		// Store the current texture mode, set overlay texture mode
		int[] texenv = new int[1];
		gl.glGetTexEnviv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv, 0);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);

		// Draw the box area
		gl.glColor4f(0f, 0.5f, 0f, 0.5f);
		gl.glBegin(GL_LINE_LOOP);
		{
			gl.glVertex2i(area.getLeft(), area.getBottom());
			gl.glVertex2i(area.getLeft(), area.getTop());
			gl.glVertex2i(area.getRight(), area.getTop());
			gl.glVertex2i(area.getRight(), area.getBottom());
		}
		gl.glEnd();

		// Restore all previous states
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv[0]);
	}
}
