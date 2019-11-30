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

package nz.net.ultraq.redhorizon.engine.graphics;

import redhorizon.engine.EngineException;
import redhorizon.engine.EngineStrings;
import redhorizon.engine.SubsystemCallback;
import redhorizon.engine.display.RenderingDelegate;
import redhorizon.engine.display.GameWindow;
import redhorizon.engine.graphics.Camera.CameraProjection;
import redhorizon.scenegraph.Scene;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Graphics subsystem, creates a display which drives the rendering loop of
 * drawing graphics objects.
 * 
 * @author Emanuel Rabina
 */
public class GraphicsSubsystem implements RenderingDelegate, Runnable {

	private final Scene scene;
	private final GameWindow window;
	private final SubsystemCallback callback;

	/**
	 * Constructor, initializes the graphics engine and attaches it to the given
	 * scene graph, display, and event listener callback.
	 * 
	 * @param scene
	 * @param window
	 * @param callback
	 */
	public GraphicsSubsystem(Scene scene, GameWindow window, SubsystemCallback callback) {

		this.scene    = scene;
		this.window   = window;
		this.callback = callback;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void displayClosed() {

		for (GraphicsEngineListener listener: listeners) {
			listener.shutdown(gl);
		}

		callback.stopRendering();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void displayInit() {

		// Set the renderer context to be valid on this thread
		renderer = new OpenGLGraphicsRenderer();
		renderer.initialize();

		callback.subsystemInit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void displayRendering() {

		// Clear color and depth buffer
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Render scene
		for (GraphicsEngineListener listener: listeners) {
			listener.rendering(gl);
		}
	}

	/**
	 * Graphics engine loop, initiates the display device from which the render
	 * loop will be controlled.
	 */
	@Override
	public void run() {

		Thread.currentThread().setName("Game Engine - Graphics Subsystem");

		GraphicsRenderer renderer = null;
		try {
			// Startup
			scene.setCamera(new Camera(CameraProjection.ORTHOGRAPHIC,
					window.getCanvasWidth(), window.getCanvasHeight(), 100));

			// Perform the rendering loop
			window.open();
		}
		finally {
			// Shutdown
			if (renderer != null) {
				renderer.cleanup();
			}
			window.close();
			callback.subsystemStop();
		}
	}
}
