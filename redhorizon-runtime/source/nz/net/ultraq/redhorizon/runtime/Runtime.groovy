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
import nz.net.ultraq.redhorizon.engine.SimulationSystem
import nz.net.ultraq.redhorizon.engine.debug.DebugCollisionOutlineSystem
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.physics.CollisionSystem
import nz.net.ultraq.redhorizon.engine.scene.SceneUpdateSystem
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.scripts.ScriptSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.runtime.utilities.ColourTypeConverter
import nz.net.ultraq.redhorizon.runtime.utilities.VersionReader
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.*

import org.lwjgl.system.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

import java.util.concurrent.Callable

/**
 * Main class for starting a Red Horizon engine, configuring and running it for
 * a given application.  Given your own {@link Application} class, usage is like
 * so:
 * <pre><code>
 * static void main(String[] args) {
 *   System.exit(new Runtime(new MyApplication()).execute(args))
 * }
 * </code></pre>
 * Available {@code args} can be found by passing {@code --help} as an arg, or
 * inspecting the {@code @Option}-annotated members of this class (it's all
 * <a href="https://picocli.info/">Picocli</a> under the hood).
 *
 * @author Emanuel Rabina
 */
@Command(name = 'runtime')
final class Runtime implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(Runtime)

	private final String version
	private final Application application

	private Window window
	private Framebuffer framebuffer
	private BasicShader shader
	private ResourceManager resourceManager
	private Scene scene

	// LWJGL options
	@Option(names = ['--lwjgl-stack-size'], defaultValue = '10240')
	int lwjglStackSize

	// Window options
	@Option(names = ['--window-background-colour'], defaultValue = 'BLACK', converter = ColourTypeConverter)
	Colour windowBackgroundColour
	@Option(names = ['--window-width'], defaultValue = '800')
	int windowWidth
	@Option(names = ['--window-height'], defaultValue = '600')
	int windowHeight

	// Framebuffer options
	@Option(names = ['--framebuffer-width'], defaultValue = '800')
	int framebufferWidth
	@Option(names = ['--framebuffer-height'], defaultValue = '600')
	int framebufferHeight

	// Simulation options
	@Option(names = ['--simulation-update-frequency'], defaultValue = '60')
	int simulationUpdateFrequency

	// Resource manager options
	@Option(names = ['--resource-manager-path-prefix'],
		description = 'Path prefix for the resource manager, defaults to the application\'s package name as a path')
	String resourceManagerPathPrefix

	/**
	 * Constructor, creates a new runtime to launch an application.
	 */
	Runtime(Application application) {

		version = new VersionReader('runtime.properties').read()
		logger.debug('Red Horizon runtime version {}, for application {} {}', version, application.name, application.version)
		this.application = application
	}

	@Override
	Integer call() {

		try {
			// Init libraries
			Configuration.STACK_SIZE.set(lwjglStackSize)

			// Init devices
			window = new OpenGLWindow(windowWidth, windowHeight, "${application.name} ${application.version}")
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(windowBackgroundColour)
				.withVSync(true)
			framebuffer = new OpenGLFramebuffer(framebufferWidth, framebufferHeight)
			shader = new BasicShader()
			var inputEventHandler = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)
			resourceManager = new ResourceManager(resourceManagerPathPrefix ?: application.class.packageName.replaceAll('\\.', '/'))

			ScopedValue
				.where(WINDOW, window)
				.where(RESOURCE_MANAGER, resourceManager)
				.run { ->

					// Init scene and systems
					scene = application.configureScene(
						new SimpleScene(framebufferWidth, framebufferHeight, window).tap {
							addDebugComponents(window, camera, inputEventHandler)
						}
					)
					var engine = new Engine()
						.addSystem(new InputSystem(inputEventHandler))
						.addSystem(new DebugCollisionOutlineSystem())
						.addSystem(new SimulationSystem(simulationUpdateFrequency)
							.addSystem(new ScriptSystem(new ScriptEngine('.'), inputEventHandler))
							.addSystem(new CollisionSystem())
							.addSystem(new SceneUpdateSystem())
						)
						.addSystem(new GraphicsSystem(window, framebuffer, shader))
						.withScene(scene)

					// Application loop
					window.show()
					var deltaTimer = new DeltaTimer()
					while (!window.shouldClose()) {
						engine.update(deltaTimer.deltaTime())
						Thread.yield()
					}
				}
		}
		catch (Throwable throwable) {
			logger.error('An error occurred', throwable)
			return 1
		}
		finally {
			scene?.close()
			resourceManager?.close()
			shader?.close()
			framebuffer?.close()
			window?.close()
		}

		return 0
	}

	/**
	 * Start the Red Horizon runtime with the given command-line parameters
	 * straight from a standard Java {@code main} method.
	 *
	 * @param args
	 *   Command line arguments for configuring the runtime.
	 * @return
	 *   A value that can be passed to `System.exit` to indicate whether the
	 *   runtime and application completed successfully, or with an error.
	 */
	int execute(String[] args) {

		logger.debug('Initializing application w/ args: {}', args)
		return new CommandLine(this).execute(args)
	}
}
