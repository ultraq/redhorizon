/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package redhorizon.engine.graphics;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL.*;

/**
 * OpenGL graphics renderer, draws graphics on the user's display via the OpenGL
 * API.
 * 
 * @author Emanuel Rabina
 */
public class OpenGLGraphicsRenderer implements GraphicsRenderer {

	private OpenGLContextManager contextmanager;
	private GL gl;

	/**
	 * Constructor, creates a new OpenGL graphics renderer.
	 */
	OpenGLGraphicsRenderer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanup() {

		contextmanager.releaseCurrentContext();
		contextmanager.destroyCurrentContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {

		contextmanager = new OpenGLContextManager();
		contextmanager.makeCurrentContext();
		gl = GLU.getCurrentGL();

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

		// Set up the viewport based on the camera settings
		gl.glViewport(0, 0, viewport.getWidth(), viewport.getHeight());
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(viewvolume.getLeft(), viewvolume.getRight(),
				   viewvolume.getBottom(), viewvolume.getTop(),
				   viewvolume.getFront(), viewvolume.getBack());
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateCamera(Camera camera) {

		// Update the position of the camera
		Point3D diff = getPosition().difference(lastpos);
		if (!diff.equals(Point3D.DEFAULT)) {
			gl.glTranslatef(-diff.getX(), -diff.getY(), -diff.getZ());
			lastpos = lastpos.add(diff);
		}
	}

	/**
	 * OpenGL context manager for the graphics engine class/thread.  For any
	 * OpenGL rendering to be done, a rendering context must be current on the
	 * executing thread.
	 */
	private class OpenGLContextManager {

		private GLContext glcontext;

		/**
		 * Constructor.
		 */
		private OpenGLContextManager() {
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
