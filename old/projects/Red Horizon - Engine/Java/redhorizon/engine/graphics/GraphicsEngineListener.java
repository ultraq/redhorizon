
// ===============================================
// Scanner's Java - Graphics engine event listener
// ===============================================

package redhorizon.engine.graphics;

import redhorizon.engine.EngineListener;

import javax.media.opengl.GL;

/**
 * Listener for graphics engine specific events.
 * 
 * @author Emanuel Rabina
 */
public interface GraphicsEngineListener extends EngineListener {

	/**
	 * Called during the graphics engine's rendering cycle.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
	public void rendering(GL gl);

	/**
	 * Called when the audio engine is being shut down.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
	public void shutdown(GL gl);
}
