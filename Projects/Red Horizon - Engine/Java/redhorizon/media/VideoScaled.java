
// ================================================
// Scanner's Java - Video stretched over the screen
// ================================================

package redhorizon.media;

import redhorizon.filetypes.VideoFile;
import redhorizon.settings.Settings;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

/**
 * A video that is scaled to fit the screen while maintaining aspect ratio.
 * 
 * @author Emanuel Rabina.
 */
public class VideoScaled extends Video {

	private static float lineopacity;

	/**
	 * Constructor, takes the necessary data from the <code>VideoFile</code> to
	 * create the video.  Videos by default do not loop.
	 * 
	 * @param videofile The file containing the video.
	 */
	public VideoScaled(VideoFile videofile) {

		super(videofile);

		// Include scanlines?
		lineopacity = Float.parseFloat(Settings.getSetting(MediaSettingsKeys.SCANLINES));
	}

	/**
	 * @inheritDoc
	 */
	Animation initAnimation(VideoFile videofile) {

		return new AnimationScaled(videofile, false, delegator);
	}

	/**
	 * @inheritDoc
	 */
	public void render(GL gl) {

		super.render(gl);

		// Draw scanlines
		if (lineopacity > 0) {

			// Save current texture environment mode
			int[] texenv = new int[1];
			gl.glGetTexEnviv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv, 0);
			gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);

			// Draw a series of horizontal black lines over the video
			gl.glColor4f(0, 0, 0, lineopacity);
			gl.glBegin(GL_LINES);
			{
				for (int i = 0; i < animation.coords.height(); i+= 2) {
					gl.glVertex2f(animation.coords.getLeft(), i);
					gl.glVertex2f(animation.coords.getRight(), i);
				}
			}
			gl.glEnd();

			// Restore the texture environment mode
			gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texenv[0]);
		}
	}
}
