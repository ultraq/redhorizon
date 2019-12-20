
// ===============================
// Scanner's Java - Status display
// ===============================

package redhorizon.game.hud;

import redhorizon.engine.scenegraph.AbstractDrawable;
import redhorizon.misc.geometry.Rectangle2D;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

/**
 * Vanilla health & status display square that gets drawn over a unit/structure
 * when it has been selected.  This type of selection box displays only the
 * square-corner selection brackets, plus a current hitpoints meter.
 * 
 * @author Emanuel Rabina
 */
public class StatusBox extends AbstractDrawable {

	// Status box constants
	private static final float HP_MIN = 0f;
	private static final float HP_MAX = 1f;
	private static final int BAR_HEIGHT = 3;

	// Status box characteristics
	private final int left;
	private final int bottom;
	private final int right;
	private final int top;
	private final int bracketwidth;
	private final int bracketheight;
	private float hp = 1.0f;

	/**
	 * Constructor, creates a status box of the specified size.
	 * 
	 * @param dimensions Rectangle with the co-ordinates required by the status
	 * 					 box.
	 */
	public StatusBox(Rectangle2D dimensions) {

		left   = dimensions.getLeft();
		bottom = dimensions.getBottom();
		right  = dimensions.getRight();
		top    = dimensions.getTop();

		bracketwidth  = dimensions.width() >> 2;
		bracketheight = dimensions.height() >> 2;
	}

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
	 * @inheritDoc
	 */
	protected Rectangle2D getBoundingArea() {

		return new Rectangle2D(left, bottom, right, top);
	}

	/**
	 * @inheritDoc
	 */
	public void render(GL gl) {

		// TODO: Put the status box on the scenegraph?  Have it's own depth
		//		 level so no need to do addition glTranslatef()?

		// Save current texture environment mode, modelview matrix
		int[] texenv = new int[1];
		gl.glGetTexEnviv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv, 0);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
		gl.glPushMatrix();
		gl.glTranslatef(0, 0, 1);

		// Draw a dark transparent backing to the health bar
		gl.glPushMatrix();
		gl.glTranslatef(left, top + 1, 0);
		gl.glBegin(GL_QUADS);
		{
			gl.glColor4f(0, 0, 0, 0.25f);
			gl.glVertex2f(1, 1);
			gl.glVertex2f(1, BAR_HEIGHT);
			gl.glVertex2f(right - 1, BAR_HEIGHT);
			gl.glVertex2f(right - 1, 1);

			gl.glColor4f(1 - hp, hp, 0, 1);
			gl.glVertex2f(1, 1);
			gl.glVertex2f(1, BAR_HEIGHT);
			gl.glVertex2f((right - 1) * hp, BAR_HEIGHT);
			gl.glVertex2f((right - 1) * hp, 1);
		}
		gl.glEnd();

		// Darkened outline bordering the health bar
		gl.glBegin(GL_LINE_LOOP);
		{
			gl.glColor4f(0, 0, 0, 1);
			gl.glVertex2f(0, 0);
			gl.glVertex2f(0, BAR_HEIGHT);
			gl.glVertex2f(right, BAR_HEIGHT);
			gl.glVertex2f(right, 0);
		}
		gl.glEnd();
		gl.glPopMatrix();

		// Draw the square brackets
		gl.glColor4f(1, 1, 1, 1);
		gl.glBegin(GL_LINE_STRIP);
		{
			gl.glVertex2f(left + bracketwidth, bottom);
			gl.glVertex2f(left, bottom);
			gl.glVertex2f(left, bottom + bracketheight);
		}
		gl.glEnd();
		gl.glBegin(GL_LINE_STRIP);
		{
			gl.glVertex2f(left, top - bracketheight);
			gl.glVertex2f(left, top);
			gl.glVertex2f(left + bracketwidth, top);
		}
		gl.glEnd();
		gl.glBegin(GL_LINE_STRIP);
		{
			gl.glVertex2f(right - bracketwidth, top);
			gl.glVertex2f(right, top);
			gl.glVertex2f(right, top - bracketheight);
		}
		gl.glEnd();
		gl.glBegin(GL_LINE_STRIP);
		{
			gl.glVertex2f(right, bottom + bracketheight);
			gl.glVertex2f(right, bottom);
			gl.glVertex2f(right - bracketwidth, bottom);
		}
		gl.glEnd();

		// Restore texture environment mode, modelview matrix
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv[0]);
		gl.glPopMatrix();
	}

	/**
	 * Sets the current hitpoints percentage the status box should be displaying.
	 * 
	 * @param hp Updated hitpoints percentage value.  Should be a value between
	 * 			 0.0 and 1.0, will be clamped to this range if provided
	 * 			 otherwise.
	 */
	public void setHPPercentage(float hp) {

		this.hp = Math.min(Math.max(hp, HP_MIN), HP_MAX);
	}
}
