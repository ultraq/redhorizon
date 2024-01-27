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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.async.ControlledLoop
import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.SystemReadyEvent
import nz.net.ultraq.redhorizon.engine.SystemStoppedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLCamera
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLContext
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.RenderPipeline
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

/**
 * Graphics system, creates a display which drives the rendering loop of drawing
 * graphics objects.
 *
 * @author Emanuel Rabina
 */
class GraphicsSystem extends EngineSystem implements GraphicsRequests, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsSystem)

	private final String windowTitle
	private final GraphicsConfiguration config
	private final InputEventStream inputEventStream
	private final ExecutorService executorService = Executors.newCachedThreadPool()
	private final BlockingQueue<Tuple2<SpriteMeshRequest, BlockingQueue<Mesh>>> meshRequests = new LinkedBlockingQueue<>()
	private final BlockingQueue<Tuple2<ShaderRequest, BlockingQueue<Shader>>> shaderRequests = new LinkedBlockingQueue<>()
	private final BlockingQueue<Tuple2<TextureRequest, BlockingQueue<Texture>>> textureRequests = new LinkedBlockingQueue<>()

	private OpenGLContext context
	private OpenGLCamera camera
	private RenderPipeline renderPipeline

	private boolean shouldToggleFullScreen
	private boolean shouldToggleVsync
	private long lastClickTime

	@Delegate
	private ControlledLoop systemLoop

	/**
	 * Constructor, build a new system for rendering graphics.
	 *
	 * @param scene
	 * @param windowTitle
	 * @param inputEventStream
	 * @param config
	 */
	GraphicsSystem(Scene scene, String windowTitle, InputEventStream inputEventStream, GraphicsConfiguration config) {

		super(scene)
		this.windowTitle = windowTitle
		this.inputEventStream = inputEventStream
		this.config = config ?: new GraphicsConfiguration()
	}

	/**
	 * Implementation of double-click being used to toggle between windowed and
	 * full screen modes.  This isn't natively supported in GLFW given platform
	 * differences in double-click behaviour, so we have to roll it ourselves.
	 *
	 * @param event
	 */
	private void checkScreenMode(MouseButtonEvent event) {

		if (event.button == GLFW_MOUSE_BUTTON_1 && event.action == GLFW_RELEASE) {
			def clickTime = System.currentTimeMillis()
			if (clickTime - lastClickTime < 300) {
				shouldToggleFullScreen = true
			}
			lastClickTime = clickTime
		}
	}

	/**
	 * Return the current camera.
	 *
	 * @return
	 */
	Camera getCamera() {

		return camera
	}

	/**
	 * Return the rendering pipeline.
	 *
	 * @return
	 */
	RenderPipeline getRenderPipeline() {

		return renderPipeline
	}

	/**
	 * Return the window that is the target for rendering.
	 *
	 * @return
	 */
	Window getWindow() {

		return context.window
	}

	/**
	 * Run through all of the queued requests for graphics resources.
	 *
	 * @param renderer
	 */
	void processRequests(GraphicsRenderer renderer) {

		if (meshRequests) {
			meshRequests.drain().each { meshRequestAndPipe ->
				def (meshRequest, pipe) = meshRequestAndPipe
				pipe.add(renderer.createSpriteMesh(surface: meshRequest.surface()))
			}
		}

		if (shaderRequests) {
			shaderRequests.drain().each { shaderRequestAndPipe ->
				def (shaderRequest, pipe) = shaderRequestAndPipe
				pipe.add(renderer.getShader(shaderRequest.name()))
			}
		}

		if (textureRequests) {
			textureRequests.drain().each { textureRequestAndPipe ->
				def (textureRequest, pipe) = textureRequestAndPipe
				pipe.add(renderer.createTexture(textureRequest.width(), textureRequest.height(),
					textureRequest.format(), textureRequest.data()))
			}
		}
	}

	/**
	 * Load a request onto the given queue, returning a future of the requested
	 * resource.
	 */
	<R extends Request<V>, V> Future<V> queueRequest(R request, BlockingQueue<Tuple2<R, BlockingQueue<V>>> requestQueue) {

		return executorService.submit({ ->
			var pipe = new LinkedBlockingQueue<V>(1)
			requestQueue.add(new Tuple2(request, pipe))
			return pipe.take()
		} as Callable<V>)
	}

	@Override
	Future<Mesh> requestMesh(SpriteMeshRequest spriteMeshRequest) {

		return queueRequest(spriteMeshRequest, meshRequests)
	}

	@Override
	Future<Shader> requestShader(ShaderRequest shaderRequest) {

		return queueRequest(shaderRequest, shaderRequests)
	}

	@Override
	Future<Texture> requestTexture(TextureRequest textureRequest) {

		return queueRequest(textureRequest, textureRequests)
	}

	/**
	 * Start the graphics system loop: creates a new window in which to render the
	 * elements in the current scene, cleaning it all up when made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Graphics System'
		logger.debug('Starting graphics system')

		// Initialization
		context = new OpenGLContext(windowTitle, config)
		context.withCloseable { context ->
			var window = context.window
			window.relay(FramebufferSizeEvent, this)
			window.relay(WindowMaximizedEvent, this)
			scene.window = window

			// Only do quick window mode switching on Windows - the macOS experience
			// is quite different from using the fullscreen button which assigns the
			// app its own space.
			if (System.isWindows()) {
				window.on(MouseButtonEvent) { event ->
					checkScreenMode(event)
				}
			}

			window.on(KeyEvent) { event ->
				if (event.action == GLFW_PRESS && event.key == GLFW_KEY_V) {
					shouldToggleVsync = true
				}
			}

			context.withCurrent { ->
				trigger(new WindowCreatedEvent(window))

				new OpenGLRenderer(config, window).withCloseable { renderer ->
					scene.graphicsRequestHandler = this

					camera = new OpenGLCamera(window.renderResolution)
					camera.withCloseable { camera ->
						new ImGuiLayer(config, window, inputEventStream).withCloseable { imGuiLayer ->
							logger.debug(renderer.toString())
							imGuiLayer.relay(FramebufferSizeEvent, this)

							renderPipeline = new RenderPipeline(config, window, renderer, imGuiLayer, inputEventStream, scene, camera)
							renderPipeline.withCloseable { pipeline ->
								trigger(new SystemReadyEvent())

								// Rendering loop
								logger.debug('Graphics system in render loop...')
								systemLoop = new ControlledLoop({ !window.shouldClose() }, { ->
									if (shouldToggleFullScreen) {
										window.toggleFullScreen()
										shouldToggleFullScreen = false
									}
									if (shouldToggleVsync) {
										window.toggleVsync()
										shouldToggleVsync = false
									}

									processRequests(renderer)
									pipeline.render()
									window.swapBuffers()
									window.pollEvents()
								})
								systemLoop.run()

								// Shutdown
								logger.debug('Shutting down graphics system')
							}
						}
					}
				}
			}
		}
		trigger(new SystemStoppedEvent())
		logger.debug('Graphics system stopped')
	}
}
