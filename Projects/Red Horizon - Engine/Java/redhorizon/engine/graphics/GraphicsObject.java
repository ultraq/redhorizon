
// ====================================================
// Scanner's Java - OpenGL item for the Graphics Engine
// ====================================================

package redhorizon.engine.graphics;

import javax.media.opengl.GL;

/**
 * Interface defining methods that are used by the {@link GraphicsSubsystem} to
 * perform initialization, rendering, and deletion of OpenGL renderables.  It is
 * in these methods that an OpenGL pipeline of a current context is guaranteed.
 * 
 * @author Emanuel Rabina
 */
public interface GraphicsObject {

	/**
	 * Deletes any resources being used by this <tt>GLDrawable</tt>.  Because of
	 * the nature of OpenGL objects, implementations can have this method do
	 * nothing at all.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
	public void delete(GL gl);

	/**
	 * Initializes this <tt>GraphicsObject</tt> by setting-up textures and other
	 * bits and pieces on the current OpenGL context.  For implementing classes,
	 * initialization of OpenGL objects should belong here, not the constructor
	 * as an OpenGL context is not guaranteed otherwise.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
	public void init(GL gl);

	/**
	 * Draws the image into the current graphics context.  This method is called
	 * from the {@link GraphicsSubsystem}'s graphics rendering thread.
	 * 
	 * NOTE: Check all implementations of this method in future for when doing
	 * 		 graphics rendering optimizations, noteably speed-ups.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
	public void render(GL gl);
}
