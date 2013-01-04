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

import redhorizon.engine.EngineException;
import redhorizon.engine.EngineStrings;
import redhorizon.engine.SubsystemCallback;
import redhorizon.engine.display.DisplayCallback;
import redhorizon.engine.display.GameWindow;
import redhorizon.scenegraph.Scene;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Graphics subsystem, creates a display which drives the rendering loop of
 * drawing graphics objects.
 * 
 * @author Emanuel Rabina
 */
public class GraphicsSubsystem implements Runnable {

	private final Scene scene;
	private final SubsystemCallback callback;

	private GameWindow gamewindow;
	private Camera camera;

	/**
	 * Constructor, initializes the graphics engine and attaches it to the given
	 * scene graph and event listener callback.
	 * 
	 * @param scene
	 * @param callback
	 */
	public GraphicsSubsystem(Scene scene, SubsystemCallback callback) {

		this.scene    = scene;
		this.callback = callback;
	}

	/**
	 * {@inheritDoc}
	 */
	public void displayClosed() {

		for (GraphicsEngineListener listener: listeners) {
			listener.shutdown(gl);
		}

		callback.stopRendering();
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
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
	 * Graphics engine loop, initiates the display device from which the render
	 * loop will be controlled.
	 */
	@Override
	public void run() {

		Thread.currentThread().setName("Game Engine - Graphics Subsystem");

		GraphicsRenderer renderer = null;
		try {
			// Startup
			gamewindow = GameWindow.createGameWindow(GraphicsSubsystem.this);
			renderer = new OpenGLGraphicsRenderer();
			renderer.initialize();
			scene.setCamera(new Camera());
			callback.subsystemInit();

			// Perform the rendering loop
			gamewindow.open();
		}
		finally {
			// Shutdown
			if (renderer != null) {
				renderer.cleanup();
			}
			if (gamewindow != null) {
				gamewindow.close();
			}
			callback.subsystemStop();
		}
	}
}
