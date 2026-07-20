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

import nz.net.ultraq.redhorizon.audio.AudioDevice
import nz.net.ultraq.redhorizon.audio.openal.OpenALAudioDevice
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.audio.AudioSystem
import nz.net.ultraq.redhorizon.engine.debug.DebugCollisionOutlineSystem
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.physics.CollisionSystem
import nz.net.ultraq.redhorizon.engine.physics.MovementSystem
import nz.net.ultraq.redhorizon.engine.physics.PhysicsSystem
import nz.net.ultraq.redhorizon.engine.scene.SceneUpdateSystem
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.scripts.ScriptSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.runtime.utilities.VersionReader
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.*

import org.joml.primitives.Rectanglef
import org.lwjgl.system.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import java.util.function.Supplier

/**
 * Main class for starting a Red Horizon engine, configuring and running it for
 * a given application.  Given your own {@link Application} class, usage is like
 * so:
 * <pre><code>
 * static void main(String[] args) {
 *   System.exit(new Runtime(new MyApplication()).execute(args))
 * }
 * </code></pre>
 * The runtime can be configured using any of the fluent API methods available,
 * before calling the {@link #execute} method.
 *
 * @author Emanuel Rabina
 */
@Builder(builderStrategy = SimpleStrategy, prefix = 'with')
final class Runtime {

	private static final Logger logger = LoggerFactory.getLogger(Runtime)

	private final String version
	private final Application application

	private Window window
	private Framebuffer framebuffer
	private List<Shader> shaders = []
	private AudioDevice audioDevice
	private ResourceManager resourceManager
	private Scene scene

	// LWJGL options
	int lwjglStackSize = 10240

	// Audio options
	float audioMasterVolume = 1f

	// Graphics options
	Colour windowBackgroundColour = Colour.BLACK
	int windowWidth = 800
	int windowHeight = 600
	int framebufferWidth
	int framebufferHeight
	Supplier<List<Shader>> additionalShaders

	// Physics options
	int physicsFixedUpdateFrequency

	// Resource manager options
	String resourceManagerPathPrefix

	/**
	 * Constructor, creates a new runtime to launch an application.
	 */
	Runtime(Application application) {

		version = new VersionReader('runtime.properties').read()
		logger.debug('Red Horizon runtime version {}, for application {} {}', version, application.name, application.version)
		this.application = application
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

		try {
			// Init libraries
			Configuration.STACK_SIZE.set(lwjglStackSize)

			// Init devices
			audioDevice = new OpenALAudioDevice()
				.withMasterVolume(audioMasterVolume)
			window = new OpenGLWindow(windowWidth, windowHeight, "${application.name} ${application.version}")
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(windowBackgroundColour)
				.withVSync(true)
			framebuffer = new OpenGLFramebuffer(framebufferWidth ?: windowWidth, framebufferHeight ?: windowHeight)
			shaders << new BasicShader()
			if (additionalShaders) {
				shaders.addAll(additionalShaders.get())
			}
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
						new SimpleScene(windowWidth, windowHeight, window).tap {
							addDebugComponents(window, camera, inputEventHandler,
								new GridLines(new Rectanglef(0f, 0f, framebufferWidth, framebufferHeight).center(), 50f,
									new Colour('Light grey', 0.85f, 0.85f, 0.85f, 1f), Colour.GREY))
						}
					)
					var engine = new Engine()
						.addSystem(new InputSystem(inputEventHandler))
						.addSystem(new ScriptSystem(new ScriptEngine('.'), inputEventHandler))
						.addSystem(new PhysicsSystem(physicsFixedUpdateFrequency)
							.addSystem(new MovementSystem())
							.addSystem(new CollisionSystem())
						)
						.addSystem(new SceneUpdateSystem())
						.addSystem(new DebugCollisionOutlineSystem())
						.addSystem(new AudioSystem())
						.addSystem(new GraphicsSystem(window, framebuffer, shaders as Shader[]))
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
			shaders*.close()
			framebuffer?.close()
			window?.close()
			audioDevice?.close()
		}

		return 0
	}
}
