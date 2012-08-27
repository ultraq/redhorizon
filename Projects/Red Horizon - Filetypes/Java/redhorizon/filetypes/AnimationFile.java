
package redhorizon.filetypes;

/**
 * Interface for animation filetypes.
 *
 * @author Emanuel Rabina
 */
public interface AnimationFile extends ImagesFile {

	/**
	 * Returns the adjustment factor for the height of the animation.  Used
	 * mainly for older file formats of C&C where the display was either 320x200
	 * or 640x400 (a 8:5 or 1.6 pixel ratio).
	 * 
	 * @return A value to multiply the height of the animation by to get the
	 * 		   right look and feel.  Returns 1 if no adjustment is necessary.
	 */
	public float adjustmentFactor();

	/**
	 * Returns the speed at which this animation should be run, in
	 * frames/second.
	 * 
	 * @return Animation speed, as the frames component of frames/second.
	 */
	public float frameRate();
}
