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
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ControlsOverlay
import nz.net.ultraq.redhorizon.engine.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import static nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent.EVENT_TYPE_STOP

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

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

	private final String version
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

		version = getResourceAsStream('runtime.properties').withBufferedReader { reader ->
			var cliProperties = new Properties()
			cliProperties.load(reader)
			var version = cliProperties.getProperty('version')
			return version == '${version}' ? '(development)' : version
		}
		this.application = application

		logger.debug('Red Horizon runtime version {} for application', version, application.class.simpleName)
	}

	/**
	 * Start the Red Horizon runtime with the given command-line parameters
	 * straight from a standard Java {@code main} method.
	 *
	 * @param args
	 *   Command line arguments.  Currently unused.  Should in future be parsed
	 *   and merged into the options object used in the {@link #execute} method.
	 * @return
	 *   A value that can be passed to `System.exit` to indicate whether the
	 *   runtime and application completed successfully, or with an error.
	 */
	int execute(String[] args) {

		try {
			logger.debug('Initializing application w/ args {}', args)

			engine = new Engine()
			scene = new Scene()

			engine << new GameLogicSystem()

			var inputSystem = new InputSystem()
			if (inputSystem) {
				inputSystem.on(KeyEvent) { event ->
					if (event.action == GLFW_PRESS && event.key == GLFW_KEY_ESCAPE) {
						stop()
					}
				}
				inputSystem.on(GuiEvent) { event ->
					if (event.type == EVENT_TYPE_STOP) {
						stop()
					}
				}
			}
			engine << inputSystem

			var audioSystem = new AudioSystem()
			engine << audioSystem

			var graphicsSystem = new GraphicsSystem("${application.name} - ${application.version}", graphicsConfiguration)
			graphicsSystem.on(EngineSystemReadyEvent) { event ->
				graphicsSystem.imGuiLayer.addOverlay(new DebugOverlay(inputSystem, true))
				graphicsSystem.imGuiLayer.addOverlay(new ControlsOverlay(inputSystem))
			}
			graphicsSystem.relay(WindowMaximizedEvent, application)
			engine << graphicsSystem

			// Start the application
			logger.debug('Starting application...')
			engine.on(EngineReadyEvent) { event ->
				engine.scene = scene
				application.start(scene)
			}
			engine.start()
		}
		catch (Throwable throwable) {
			logger.error('An error occurred', throwable)
			return 1
		}
		finally {
			// Check we closed everything
			var check = { int resourceCount, String resourceName ->
				if (resourceCount > 0) {
					logger.warn("Not all {} closed, {} remaining", resourceName, resourceCount)
					return false
				}
				return true
			}
			var engineStats = EngineStats.instance
			var resourcesClosed = true
			resourcesClosed |= check(engineStats.activeFramebuffers.get(), 'framebuffers')
			resourcesClosed |= check(engineStats.activeMeshes.get(), 'meshes')
			resourcesClosed |= check(engineStats.activeTextures.get(), 'textures')
			resourcesClosed |= check(engineStats.activeUniformBuffers.get(), 'uniform buffers')
			resourcesClosed |= check(engineStats.activeSources.get(), 'sources')
			resourcesClosed |= check(engineStats.activeBuffers.get(), 'buffers')
			if (!resourcesClosed) {
				return 2
			}
		}

		return 0
	}

	/**
	 * Stop the application.
	 */
	final void stop() {

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
