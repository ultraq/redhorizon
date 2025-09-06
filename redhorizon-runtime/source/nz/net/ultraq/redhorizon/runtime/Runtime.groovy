/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.runtime

import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.EngineReadyEvent
import nz.net.ultraq.redhorizon.engine.EngineStats
import nz.net.ultraq.redhorizon.engine.EngineSystemReadyEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioSystem
import nz.net.ultraq.redhorizon.engine.game.GameLogicSystem
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.runtime.imgui.ControlsOverlay
import nz.net.ultraq.redhorizon.runtime.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.runtime.imgui.ExitEvent
import nz.net.ultraq.redhorizon.runtime.imgui.MainMenuBar
import nz.net.ultraq.redhorizon.runtime.imgui.MainMenuBar.MenuItem
import nz.net.ultraq.redhorizon.runtime.utilities.VersionReader

import imgui.ImGui
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

import java.util.concurrent.Semaphore

/**
 * Main class for starting a Red Horizon engine, configuring and running it for
 * a given application.  Given your own {@link Application} class, usage is like
 * so:
 * <pre><code>
 * static void main(String[] args) {
 *   System.exit(new Runtime(new MyApplication()).execute(args))
 * }
 * </code></pre>
 *
 * @author Emanuel Rabina
 */
final class Runtime {

	private static final Logger logger = LoggerFactory.getLogger(Runtime)

	final String version
	private final Application application

	private GraphicsConfiguration graphicsConfiguration
	private boolean applicationStopping
	private Semaphore applicationStoppingSemaphore = new Semaphore(1)
	private Engine engine
	private Scene scene

	/**
	 * Constructor, creates a new runtime to launch the given application.
	 */
	Runtime(Application application) {

		version = new VersionReader('runtime.properties').read()
		logger.debug('Red Horizon runtime version {} for application', version, application.class.simpleName)

		this.application = application
	}

	/**
	 * Start the Red Horizon runtime with the given command-line parameters
	 * straight from a standard Java {@code main} method.
	 *
	 * @param args
	 *   Command line arguments.  Currently unused.  Should in future be parsed
	 *   and used to configure the runtime.
	 * @return
	 *   A value that can be passed to `System.exit` to indicate whether the
	 *   runtime and application completed successfully, or with an error.
	 */
	int execute(String[] args) {

		logger.debug('Initializing application w/ args {}', args)

		try {
			engine = new Engine()
			scene = new Scene()

			// Have the Esc key close the application
			var inputSystem = new InputSystem()
			inputSystem.on(KeyEvent) { event ->
				if (event.keyPressed(GLFW_KEY_ESCAPE)) {
					stop()
				}
			}
			engine << inputSystem

			engine << new GameLogicSystem()
			engine << new AudioSystem()

			var graphicsSystem = new GraphicsSystem("${application.name} - ${application.version}", graphicsConfiguration)
			graphicsSystem.on(EngineSystemReadyEvent) { systemReadyEvent ->
				var imGuiLayer = graphicsSystem.imGuiLayer

				// Close the application when Exit in the main menu is selected
				var mainMenuBar = new MainMenuBar()
				mainMenuBar.on(ExitEvent) { exitEvent ->
					stop()
				}
				imGuiLayer << mainMenuBar

				// Add debug overlay, toggle with D key
				var debugOverlay = new DebugOverlay(inputSystem, true)
				imGuiLayer << debugOverlay
				mainMenuBar.addOptionsMenuItem(new MenuItem() {
					@Override
					void render() {
						if (ImGui.menuItem('Debug overlay', 'D', debugOverlay.enabled)) {
							debugOverlay.toggle()
						}
					}
				})

				// Add controls overlay, toggle with C key
				var controlsOverlay = new ControlsOverlay(inputSystem)
				imGuiLayer << controlsOverlay
				mainMenuBar.addOptionsMenuItem(new MenuItem() {
					@Override
					void render() {
						if (ImGui.menuItem('Controls overlay', 'C', controlsOverlay.enabled)) {
							controlsOverlay.toggle()
						}
					}
				})
			}
			graphicsSystem.relay(WindowMaximizedEvent, application)
			engine << graphicsSystem

			// Start the application
			logger.debug('Starting application...')
			engine.on(EngineReadyEvent) { event ->
				engine.scene = scene
				application.start(scene, graphicsSystem.imGuiLayer)
			}
			engine.start()
		}
		catch (Throwable throwable) {
			logger.error('An error occurred', throwable)
			return 1
		}

		// Check we closed everything
		finally {
			var check = { int resourceCount, String resourceName ->
				if (resourceCount > 0) {
					logger.warn("Not all {} closed, {} remaining", resourceName, resourceCount)
					return false
				}
				return true
			}
			var engineStats = EngineStats.instance
			var resourcesClosed = true
			resourcesClosed &= check(engineStats.activeFramebuffers.get(), 'framebuffers')
			resourcesClosed &= check(engineStats.activeMeshes.get(), 'meshes')
			resourcesClosed &= check(engineStats.activeTextures.get(), 'textures')
			resourcesClosed &= check(engineStats.activeUniformBuffers.get(), 'uniform buffers')
			resourcesClosed &= check(engineStats.activeSources.get(), 'sources')
			resourcesClosed &= check(engineStats.activeBuffers.get(), 'buffers')
			if (!resourcesClosed) {
				return 2
			}
		}

		return 0
	}

	/**
	 * Stop the application.
	 */
	void stop() {

		applicationStoppingSemaphore.tryAcquireAndRelease { ->
			if (!applicationStopping) {
				logger.debug('Stopping application...')
				application.stop(scene)
				engine.stop()
				applicationStopping = true
			}
		}
	}

	/**
	 * Configure the graphics options for this runtime.
	 */
	Runtime withGraphicsConfiguration(GraphicsConfiguration graphicsConfiguration) {

		this.graphicsConfiguration = graphicsConfiguration
		return this
	}
}
