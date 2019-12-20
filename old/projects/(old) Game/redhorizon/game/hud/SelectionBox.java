
// ===================================
// Scanner's Java - Unit selection box
// ===================================

package redhorizon.game.hud;

import redhorizon.engine.scenegraph.AbstractDrawable;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Rectangle2D;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

/**
 * Modifiable rectangle-esque selection area.  This is the rectangle you see
 * when you click-and-drag the mouse over an area in the game.  The box can
 * determine the area it has selected, and return them to the proper game
 * sub-engines for action.
 * 
 * @author Emanuel Rabina
 */
public class SelectionBox extends AbstractDrawable {

	private static SelectionBox selectionbox;

	// Selection box attributes
	private Point2D origin;
	private int width;
	private int height;

	/**
	 * Hidden default constructor, to help ensure singleton selection box
	 * instance.
	 */
	private SelectionBox() {
	}

	/**
	 * Clears the selection box from the screen and resets the origin.
	 */
	public void clearSelection() {

		origin = null;
		width  = 0;
		height = 0;
	}

	/**
	 * @inheritDoc
	 */
	public void delete(GL gl) {
	}

	/**
	 * @inheritDoc
	 */
	protected Rectangle2D getBoundingArea() {

		return selectionBoxAsRectangle();
	}

	/**
	 * Builds and returns a new <code>SelectionBox</code>.
	 * 
	 * @return A new selection box.
	 */
	public static SelectionBox createSelectionBox() {

		selectionbox = new SelectionBox();
		return selectionbox;
	}

	/**
	 * Retrieves the current instance of the <code>SelectionBox</code>.
	 * 
	 * @return Currently used selection box, or <code>null</code> if there is no
	 * 		   available selection.
	 */
	public static SelectionBox currentSelectionBox() {

		return selectionbox;
	}

	/**
	 * Returns the area encompassed by the selection box.
	 * 
	 * @return Selection area.
	 */
	public Rectangle2D getSelectionArea() {

		return selectionBoxAsRectangle();
	}

	/**
	 * Returns a rectangle representative of the current selection area.
	 * 
	 * @return Selection area as a rectangle.
	 */
	private Rectangle2D selectionBoxAsRectangle() {

		int left   = origin.getX();
		int bottom = origin.getY();
		int right  = width;
		int top    = height;

		// Ensure selection rectangle is the right way around
		if (width < 0) {
			left += width;
			right = -width;
		}
		if (height < 0) {
			bottom += height;
			top = -height;
		}

		return new Rectangle2D(left, bottom, right, top);
	}

	/**
	 * @inheritDoc
	 */
	public void init(GL gl) {
	}

	/**
	 * @inheritDoc
	 */
	public boolean isDrawing() {

		return super.isDrawing() && (width != 0 || height != 0);
	}

	/**
	 * @inheritDoc
	 */
	public void render(GL gl) {

		// Save current texture environment mode
		int[] texenv = new int[1];
		gl.glGetTexEnviv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv, 0);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);

		int coordx = origin.getX();
		int coordy = origin.getY();
		int coordw = coordx + width;
		int coordh = coordy + height;

		// Draw a transparent selection area
		gl.glBegin(GL_QUADS);
		{
			gl.glColor4f(0, 0, 0, 0.25f);
			gl.glVertex2f(coordx, coordy);
			gl.glVertex2f(coordx, coordh);
			gl.glVertex2f(coordw, coordh);
			gl.glVertex2f(coordw, coordy);
		}
		gl.glEnd();

		// Draw the outline
		gl.glBegin(GL_LINE_LOOP);
		{
			gl.glColor4f(1, 1, 1, 1);
			gl.glVertex2f(coordx, coordy);
			gl.glVertex2f(coordx, coordh);
			gl.glVertex2f(coordw, coordh);
			gl.glVertex2f(coordw, coordy);
		}
		gl.glEnd();

		// Restore texture environment mode
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv[0]);
	}

	/**
	 * Sets a new starting point for the selection box.
	 * 
	 * @param origin Co-ordinates of the new starting point.
	 */
	public void setOrigin(Point2D origin) {

		this.origin = origin;
	}

	/**
	 * Sets the width and height of the selection area from the origin.
	 * 
	 * @param width	 Width of the selection box.
	 * @param height Height of the selection box.
	 */
	public void setSelection(int width, int height) {

		this.width  = width;
		this.height = height;
	}
}
