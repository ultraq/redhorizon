
// ====================================
// Scanner's Java - Graphics sub-engine
// ====================================

package redhorizon.engine.graphics;

import redhorizon.engine.Engine;
import redhorizon.engine.EngineException;
import redhorizon.engine.EngineStrings;
import redhorizon.engine.display.DisplayCallback;
import redhorizon.engine.display.GameWindow;
import redhorizon.strings.Strings;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL.*;

import java.util.Arrays;
import java.util.HashSet;

/**
 * A sub-section of the overall game engine, the graphics engine is the part
 * that initializes the graphics sub-system of the game, as well as drives the
 * frame rendering loop of the OpenGL display/context.
 * 
 * @author Emanuel Rabina
 */
public class GraphicsEngine extends Engine<GraphicsEngineCallback,GraphicsEngineListener>
	implements DisplayCallback {

	public static final float OPENGL_MIN_REQ_VER = 1.3f;
	public static final String[] OPENGL_REQ_EXTENSIONS = { "GL_ARB_texture_env_combine" };

	// Graphics engine rendering wait period
	private static long GRAPHICS_ENGINE_BACKOFF_PERIOD = 5;

	private HashSet<String> openglfeatures = new HashSet<String>();
	private GLContextManager contextmanager;
	private GL gl;

	private GameWindow gamewindow;
	private Camera camera;

	/**
	 * Constructor, initializes the display engine.
	 * 
	 * @param callback Main engine.
	 */
	public GraphicsEngine(GraphicsEngineCallback callback) {

		super(callback);
	}

	/**
	 * @inheritDoc
	 */
	protected long backoffPeriod() {

		return GRAPHICS_ENGINE_BACKOFF_PERIOD;
	}

	/**
	 * @inheritDoc
	 */
	public void displayClosed() {

		for (GraphicsEngineListener listener: listeners) {
			listener.shutdown(gl);
		}

		callback.stopRendering();
	}

	/**
	 * @inheritDoc
	 */
	public void displayInit() {

		// Create context and pipeline once window is set up
		contextmanager = new GLContextManager();
		contextmanager.makeCurrentContext();
		gl = GLU.getCurrentGL();

		testGL();
		enableGL();

		// Create viewport, camera, attach to render window
		camera = new Camera(gl, gamewindow.getRenderingArea());
		addListener(camera);

		callback.initRendering();
	}

	/**
	 * @inheritDoc
	 */
	public void displayRendering() {

		// Clear color and depth buffer
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Render scene
		for (GraphicsEngineListener listener: listeners) {
			listener.rendering(gl);
		}
	}

	/**
	 * Initializes the OpenGL device, context, and rendering pipeline.
	 */
	@Override
	protected void doInit() {

		gamewindow = GameWindow.createGameWindow(GraphicsEngine.this);
	}

	/**
	 * Does nothing for the graphics engine (update is controlled by the UI
	 * thread).
	 */
	@Override
	protected void doRun() {

	}

	/**
	 * Shuts-down the rendering target (eg: the {@link GameWindow}), notifies
	 * graphics listeners of shutdown, and closes the OpenGL context and
	 * rendering pipeline.
	 * 
	 * @see #addListener(GraphicsEngineListener)
	 */
	@Override
	protected void doShutdown() {

		if (gamewindow != null) {
			gamewindow.close();
		}
		if (contextmanager != null) {
			contextmanager.releaseCurrentContext();
			contextmanager.destroyCurrentContext();
		}
	}

	/**
	 * Enables any required OpenGL features depending upon the settings.
	 */
	private void enableGL() {

		gl.glClearColor(0, 0, 0, 1);

		// Edge smoothing
		gl.glHint(GL_POINT_SMOOTH_HINT, GL_FASTEST);
		gl.glHint(GL_LINE_SMOOTH_HINT, GL_FASTEST);
		gl.glHint(GL_POLYGON_SMOOTH_HINT, GL_FASTEST);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);

		// Disable antialiasing globally
		if (gl.isExtensionAvailable("GL_ARB_multisample")) {
			gl.glDisable(GL_MULTISAMPLE);
		}

		// Texturing controls
		gl.glEnable(GL_TEXTURE_2D);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

		// Texture blend combo, create a mixture of GL_BLEND on RGB, GL_REPLACE on A
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_INTERPOLATE);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_REPLACE);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_RGB, GL_PRIMARY_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA, GL_PRIMARY_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE1_RGB, GL_PRIMARY_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_RGB, GL_TEXTURE);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_RGB, GL_SRC_COLOR);

		// Depth testing
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		// Alpha testing
		gl.glEnable(GL_ALPHA_TEST);
		gl.glAlphaFunc(GL_GREATER, 0);

		// Blending and blending function
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	/**
	 * Returns the camera being used to render to the current viewport.
	 * 
	 * @return The camera for the current viewport.
	 */
	public Camera getCurrentCamera() {

		return camera;
	}

	/**
	 * Returns the current game window being rendered to.
	 * 
	 * @return The current rendering target (window).
	 */
	public GameWindow getCurrentGameWindow() {

		return gamewindow;
	}

	/**
	 * Graphics-specific execution.  The control must be given to the UI thread
	 * (SWT display handle).
	 */
	public void run() {

		try {
			// Startup
			doInit();

			// Running
			gamewindow.open();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
		finally {
			// Shutdown
			doShutdown();
		}
	}

	/**
	 * @inheritDoc
	 */
	public void stop() {

		if (gamewindow != null) {
			gamewindow.close();
		}
	}

	/**
	 * Test OpenGL hardware if it meets the minimum version specified in
	 * {@link #OPENGL_MIN_REQ_VER} and has any of the required extensions listed
	 * in {@link #OPENGL_REQ_EXTENSIONS}.
	 * 
	 * @throws EngineException If the computer fails the video test.
	 */
	private void testGL() throws EngineException {

		// Check the OpenGL version number
		// NOTE: Dump the version check? (regular expression pattern check too complex)
/*		String glversionstring = gl.glGetString(GL_VERSION);
		boolean versionmet = false;

		for (String versionword: glversionstring.split(" ")) {
			if (versionword.length() >= 3) {
				String versionpart = versionword.substring(0, 3);
				if (versionpart.matches("[1-9].[0-9]") && Float.parseFloat(versionpart) >= OPENGL_MIN_REQ_VER) {
					versionmet = true;
					break;
				}
			}
		}
		if (!versionmet) {
			throw new MissingCapabilityException(Strings.getText(
					Game_Errors.OPENGL_VERSION_NOT_MET, Float.toString(OPENGL_MIN_REQ_VER), glversionstring));
		}
*/
		// Check the required OpenGL extensions
		for (String ext: OPENGL_REQ_EXTENSIONS) {
			if (!gl.isExtensionAvailable(ext)) {
				throw new EngineException(Strings.getText(EngineStrings.OPENGL_EXTENSION_NOT_FOUND, ext));
			}
		}

		// Query and store the platform's OpenGL capabilities (extensions)
		String glextensions = gl.glGetString(GL_EXTENSIONS);
		String[] glextlist = glextensions.split(" ");
		Arrays.sort(glextlist);
		for (String glext: glextlist) {
			if (glext.startsWith("GL_ARB") || glext.startsWith("GL_EXT")) {
				openglfeatures.add(glext);
			}
		}
	}

	/**
	 * OpenGL context manager for the graphics engine class/thread.  For any
	 * OpenGL rendering to be done, a rendering context must be current on the
	 * executing thread.  This class helps take care of that.
	 * 
	 * @author Emanuel Rabina
	 */
	private class GLContextManager {

		private GLContext glcontext;

		/**
		 * Default constructor.
		 */
		private GLContextManager() {
		}

		/**
		 * Destroys the OpenGL context for the currently executing thread.  The
		 * context should first be released before destruction.
		 */
		private void destroyCurrentContext() {

			if (glcontext != null) {
				glcontext.destroy();
				glcontext = null;
			}
		}

		/**
		 * Returns the OpenGL context for the currently executing thread.  If
		 * there is no context, then a new one is created, then made current.
		 */
		private void makeCurrentContext() {

			// Use the current context, or make a new one
			if (glcontext == null) {
				glcontext = GLDrawableFactory.getFactory().createExternalGLContext();
			}
			glcontext.makeCurrent();
		}

		/**
		 * Releases the OpenGL context that is current on the executing thread.
		 */
		private void releaseCurrentContext() {

			if (glcontext != null) {
				glcontext.release();
			}
		}
	}
}
