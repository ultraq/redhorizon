
// ====================================================
// Scanner's Java - Animation stretched over the screen
// ====================================================

package redhorizon.media;

import redhorizon.engine.Camera;
import redhorizon.filetypes.AnimationFile;
import redhorizon.misc.geometry.Rectangle2D;

/**
 * An animation that is stretched to fit the screen.  Animations of this type
 * are scaled (maintaining aspect ratio) to the size of the viewport.
 * 
 * @author Emanuel Rabina.
 */
public class AnimationScaled extends Animation {

	/**
	 * Constructor, takes the necessary data from the <code>AnimationFile</code>
	 * to create an animation.  Animations by default close when completed and
	 * do not loop.
	 * 
	 * @param animationfile The file containing the animation.
	 */
	public AnimationScaled(AnimationFile animationfile) {

		this(animationfile, false);
	}

	/**
	 * Constructor, takes the necessary data from the <code>AnimationFile</code>
	 * to create an animation.  Specifies close-on-exit and loop
	 * characteristics.
	 * 
	 * @param animationfile The file containing the animation.
	 * @param looping		Whether to have this animation loop continuously.
	 */
	public AnimationScaled(AnimationFile animationfile, boolean looping) {

		this(animationfile, looping, null);
	}

	/**
	 * Constructor, specifies an <code>AnimationDelegator</code>.
	 * 
	 * @param animationfile The file containing the animation.
	 * @param looping		Whether to have this animation loop continuously.
	 * @param delegator		Should the internal delegator be used?
	 */
	AnimationScaled(AnimationFile animationfile, boolean looping, AnimationDelegator delegator) {

		super(animationfile, looping, delegator);

		// Alter widths and heights to fit screen and maintain aspect ratio
		Rectangle2D viewarea = Camera.currentCamera().getProjectionVolume();
		float viewwidth  = viewarea.width();
		float viewheight = viewarea.height();
		float aspectratio = viewwidth / viewheight;

		float animwidth  = animationfile.width();
		float animheight = animationfile.height();

		float modifier = (animwidth / animheight >= aspectratio) ?
				viewwidth / animwidth:
				viewheight / animheight;

		coords = coords.scale(modifier, modifier * animationfile.adjustmentFactor());
	}
}
