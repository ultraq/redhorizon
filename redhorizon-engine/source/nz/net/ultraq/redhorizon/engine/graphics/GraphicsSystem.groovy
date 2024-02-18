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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
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
	private final BlockingQueue<Tuple2<Request, CompletableFuture<GraphicsResource>>> creationRequests = new LinkedBlockingQueue<>()
	private final BlockingQueue<GraphicsResource> deletionRequests = new LinkedBlockingQueue<>()

	private OpenGLContext context
	private OpenGLRenderer renderer
	private OpenGLCamera camera
	private RenderPipeline renderPipeline

	private boolean shouldToggleFullScreen
	private boolean shouldToggleVsync
	private long lastClickTime

	/**
	 * Constructor, build a new system for rendering graphics.
	 */
	GraphicsSystem(String windowTitle, InputEventStream inputEventStream, GraphicsConfiguration config) {

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

	@Override
	void configureScene() {

		scene.window = context.window
		scene.graphicsRequestHandler = this
		scene.camera = camera
		renderPipeline.scene = scene
	}

	/**
	 * Return the renderer.
	 *
	 * @return
	 */
	GraphicsRenderer getRenderer() {

		return renderer
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
	 * Run through all of the queued requests for the creation and deletion of
	 * graphics resources.
	 *
	 * @param renderer
	 */
	void processRequests(GraphicsRenderer renderer) {

		if (deletionRequests) {
			deletionRequests.drain().each { deletionRequest ->
				renderer.delete(deletionRequest)
			}
		}

		if (creationRequests) {
			creationRequests.drain().each { creationRequest ->
				def (request, future) = creationRequest
				var resource = switch (request) {
					case ShaderRequest -> renderer.createShader(request.shaderConfig())
					case MeshRequest -> renderer.createMesh(request.type(), request.layout(), request.colour(), request.vertices(), null, request.indices())
					case SpriteMeshRequest -> renderer.createSpriteMesh(request.surface())
					case TextureRequest -> renderer.createTexture(request.width(), request.height(), request.format(), request.data())
					default -> throw new IllegalArgumentException("Cannot create resource from type ${request}")
				}
				future.complete(resource)
			}
		}
	}

	@Override
	<V extends GraphicsResource, R extends Request<V>> Future<V> requestCreateOrGet(R request) {

		var future = new CompletableFuture<V>()
		creationRequests << new Tuple2(request, future)
		return future
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

				renderer = new OpenGLRenderer(config, window)
				renderer.withCloseable { renderer ->

					camera = new OpenGLCamera(window.renderResolution)
					camera.withCloseable { camera ->
						new ImGuiLayer(config, window).withCloseable { imGuiLayer ->
							logger.debug(renderer.toString())
							imGuiLayer.relay(FramebufferSizeEvent, this)

							renderPipeline = new RenderPipeline(config, window, renderer, imGuiLayer, inputEventStream)
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
