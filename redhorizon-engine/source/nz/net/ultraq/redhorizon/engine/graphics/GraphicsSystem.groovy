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
class GraphicsSystem extends EngineSystem implements GraphicsRequests {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsSystem)

	private final String windowTitle
	private final GraphicsConfiguration config
	private final InputEventStream inputEventStream
	private final ExecutorService executorService = Executors.newCachedThreadPool()
	private final BlockingQueue<Tuple2<Request, BlockingQueue>> creationRequests = new LinkedBlockingQueue<>()
	private final BlockingQueue<GraphicsResource> deletionRequests = new LinkedBlockingQueue<>()

	private OpenGLContext context
	private OpenGLCamera camera
	private RenderPipeline renderPipeline

	private boolean shouldToggleFullScreen
	private boolean shouldToggleVsync
	private long lastClickTime

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
	 * Run through all of the queued requests for the creation and deletion of
	 * graphics resources.
	 *
	 * @param renderer
	 */
	void processRequests(GraphicsRenderer renderer) {

		if (deletionRequests) {
			deletionRequests.drain().each { deletionRequest ->
				switch (deletionRequest) {
					case Material -> renderer.deleteMaterial(deletionRequest)
					case Mesh -> renderer.deleteMesh(deletionRequest)
					case Texture -> renderer.deleteTexture(deletionRequest)
					default -> throw new IllegalArgumentException("Cannot delete resource of type ${deletionRequest}")
				}
			}
		}

		if (creationRequests) {
			creationRequests.drain().each { creationRequest ->
				def (request, pipe) = creationRequest
				var resource = switch (request) {
					case ShaderRequest -> renderer.getShader(request.name())
					case SpriteMeshRequest -> renderer.createSpriteMesh(surface: request.surface())
					case TextureRequest -> renderer.createTexture(request.width(), request.height(), request.format(), request.data())
					default -> throw new IllegalArgumentException("Cannot create resource from type ${request}")
				}
				pipe.add(resource)
			}
		}
	}

	@Override
	<V extends GraphicsResource, R extends Request<V>> Future<V> requestCreateOrGet(R request) {

		return executorService.submit({ ->
			var pipe = new LinkedBlockingQueue<V>(1)
			creationRequests.add(new Tuple2(request, pipe))
			return pipe.take()
		} as Callable<V>)
	}

	@Override
	void requestDelete(GraphicsResource... resource) {

		deletionRequests.addAll(resource)
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
								while (!window.shouldClose() && !Thread.interrupted()) {
									try {
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
									}
									catch (InterruptedException ignored) {
										break
									}
								}

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
