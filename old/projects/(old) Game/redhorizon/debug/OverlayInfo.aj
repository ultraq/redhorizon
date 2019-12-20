
// ============================================
// Scanner's AspectJ - Displays some debug info
// ============================================

package redhorizon.debug;

import redhorizon.engine.AudioEngine;
import redhorizon.engine.Camera;
import redhorizon.engine.InputEngine;
import redhorizon.engine.scenegraph.Drawable;
import redhorizon.engine.scenegraph.Playable;
import redhorizon.engine.scenegraph.SceneManager;
import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Rectangle2D;

import static redhorizon.game.GameObjectDepths.DEPTH_OVERLAY_INFO;

import com.sun.opengl.util.GLUT;
import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

import java.util.Iterator;

/**
 * This aspect creates a small section of the lower-left display where debug
 * info is displayed.  Things that are displayed include an FPS counter, image
 * cache sizes, sounds playing, etc.
 * 
 * @author Emanuel Rabina
 */
public privileged aspect OverlayInfo {

	private static final int font = GLUT.BITMAP_8_BY_13;
	private static GLUT glut;
	private static long timer = System.currentTimeMillis();

	private static Point2D camerapos = Point2D.DEFAULT;
	private static int viewwidth;

	/**
	 * Remembers the camera viewport dimensions.
	 */
	before():
		execution(public void SceneManager.renderScene(GL)) {

		viewwidth = Camera.currentCamera().getProjectionVolume().width();
	}

	/**
	 * Remembers the position of the camera before rendering.
	 */
	before():
		execution(public void SceneManager.renderScene(GL)) {

		camerapos = Camera.currentCamera().getPosition();
	}

	/**
	 * Causes the debug info display to be called at the end of the rendering
	 * loop.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
	after(GL gl) returning:
		execution(public void SceneManager.renderScene(GL)) && args(gl) {

		ImageStats.vidcycles++;

		// Reset data every second, otherwise accumulate it
		long timenow = System.currentTimeMillis();
		boolean reset = timenow - timer >= 1000;
		if (reset) {
			timer = timenow;
		}

		// GLUT library for this thread
		if (glut == null) {
			glut = new GLUT();
		}

		// Save current texture environment mode, modelview matrix
		int[] texenv = new int[1];
		gl.glGetTexEnviv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv, 0);
		gl.glPushMatrix();
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);

		// Reposition to the overlay layer
		Rectangle2D viewarea = Camera.currentCamera().getCurrentProjection();
		gl.glTranslatef(viewarea.getLeft(), viewarea.getBottom(), DEPTH_OVERLAY_INFO);

		// Draw darkened text area
		gl.glBegin(GL_QUADS);
		{
			gl.glColor4f(0, 0, 0, 0.6f);
			gl.glVertex2f(0, 19);
			gl.glVertex2f(viewwidth, 19);
			gl.glVertex2f(viewwidth, 0);
			gl.glVertex2f(0, 0);
		}
		gl.glEnd();

		// Debug display header, colour
		gl.glTranslatef(0, 0, 0.5f);
		gl.glColor4f(0, 1, 1, 1);
		gl.glRasterPos2f(5.375f, 5.375f);

		// Display FPS, image stats, sound stats, mouse position
		FPScounter.displayFPS(reset);
		glut.glutBitmapString(font, " - ");
		ImageStats.displayStats(reset);
		glut.glutBitmapString(font, " - ");
		SoundStats.displayStats(reset);
		glut.glutBitmapString(font, " - ");
		MouseStatus.displayPosition();

		// Restore the texture environment mode, modelview matrix
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv[0]);
		gl.glPopMatrix();
	}

	/**
	 * Has an audio counter asynchronous to the graphics timer.
	 */
	after():
		execution(private void AudioEngine.audioRendering()) {

		SoundStats.audcycles++;
	}

	/**
	 * Gets the number of items drawn from the <code>GraphicsEngine</code>.
	 * Sets that value in the <code>ImageStats</code> class.
	 */
	after():
		call(public void Drawable.render(..)) && within(SceneNode) {

		ImageStats.numdrawn++;
	}

	/**
	 * Gets the number of items played from the <code>AudioEngine</code>.  Sets
	 * that value in the <code>SoundStats</code> class.
	 */
	after():
		call(public void Playable.render(..)) && within(SceneNode) {

		SoundStats.numplayed++;
	}

	/**
	 * Frames-per-second counter and inner class, calculates and displays the
	 * FPS somewhere in the OpenGL canvas.
	 */
	private static class FPScounter {

		private static int counter;
		private static int lastcount;

		/**
		 * Displays the frames-per-second somewhere on the display.
		 * 
		 * @param reset Whether or not to reset stats back to 0;
		 */
		private static void displayFPS(boolean reset) {

			// Write framerate
			glut.glutBitmapString(font, lastcount + "fps");

			// Update FPS or reset
			if (reset) {
				lastcount = counter;
				counter = 1;
			}
			else {
				counter++;
			}
		}
	}

	/**
	 * Image statistics inner class, displays a variety of image-related data,
	 * namely the number of items actually being drawn against the number of
	 * items eligible for being drawn.
	 */
	private static class ImageStats {

		private static float numdrawn;
		private static int lastdrawn;
		private static int vidcycles = 1;

		/**
		 * Displays the number of items being drawn / total drawable objects.
		 * 
		 * @param reset Whether or not to reset stats back to 0;
		 */
		private static void displayStats(boolean reset) {

			// Write drawables
			glut.glutBitmapString(font, lastdrawn + "GLs");

			if (reset) {
				lastdrawn = Math.round(numdrawn / vidcycles);
				numdrawn = 0;
				vidcycles = 1;
			}
		}
	}

	/**
	 * Sound statistics inner class, displays a variety of sound-related data,
	 * namely the number of items being played against the number of items
	 * eligible for play.
	 */
	private static class SoundStats {

		private static float numplayed;
		private static int lastplayed;
		private static int audcycles = 1;

		/**
		 * Displays the number of items beind played / total playable objects.
		 * 
		 * @param reset Whether or not to reset stats back to 0;
		 */
		private static void displayStats(boolean reset) {

			// Write playables
			glut.glutBitmapString(font, lastplayed + "ALs");

			if (reset) {
				lastplayed = Math.round(numplayed / audcycles);
				numplayed = 0;
				audcycles = 1;
			}
		}
	}

	/**
	 * Updates the mouse position.
	 * 
	 * @param mouseevent The mouseevent being read.
	 */
	after() returning(InputEngine.MouseEvent mouseevent):
		call(public * Iterator.next()) && withincode(private void InputEngine.eventHandling()) {

		// Retain Integer.[MIN|MAX]_VALUE numbers
		int posx = mouseevent.x;
		if (posx != Integer.MIN_VALUE && posx != Integer.MAX_VALUE) {
			posx += camerapos.getX();
		}
		int posy = mouseevent.y;
		if (posy != Integer.MIN_VALUE && posy != Integer.MAX_VALUE) {
			posy += camerapos.getY();
		}

		MouseStatus.position = new Point2D(posx, posy);

		// Mouse clicked-down or not
		if (mouseevent.action.equals(InputEngine.MouseAction.MOUSE_DOWN)) {
			MouseStatus.selecting = true;
		}
		else if (mouseevent.action.equals(InputEngine.MouseAction.MOUSE_UP)) {
			MouseStatus.selecting = false;
		}
	}

	/**
	 * Mouse position and state class, tracks the current mouse state so that
	 * info can be rendered somewhere on the display area.
	 */
	private static class MouseStatus {

		private static Point2D position = new Point2D(0,0);
		private static boolean selecting;

		/**
		 * Renders to the screen, the current position of the mouse.
		 */
		private static void displayPosition() {

			// Write mouse position
			if (selecting) {
				glut.glutBitmapString(font, position + "+CLICK");
			}
			else {
				glut.glutBitmapString(font, position.toString());
			}
		}
	}
}
