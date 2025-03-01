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

import nz.net.ultraq.redhorizon.engine.EngineStats
import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLContext
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.RenderPipeline
import nz.net.ultraq.redhorizon.engine.input.GamepadStateProcessor
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CyclicBarrier
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
	private final InputEventStream inputEventStream
	private final GraphicsConfiguration config
	private final BlockingQueue<Tuple2<Request, CompletableFuture<GraphicsResource>>> creationRequests = new LinkedBlockingQueue<>()
	private final BlockingQueue<Tuple2<GraphicsResource, CompletableFuture<Void>>> deletionRequests = new LinkedBlockingQueue<>()
	private final CyclicBarrier renderBarrier = new CyclicBarrier(2)
	private final CyclicBarrier continueBarrier = new CyclicBarrier(2)

	private OpenGLContext context
	private OpenGLWindow window
	private OpenGLRenderer renderer
	private ImGuiLayer imGuiLayer
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
		this.config = config
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

		scene.graphicsRequestHandler = this
		scene.window = window
		scene.gameMenu = imGuiLayer.mainMenu
		scene.gameWindow = imGuiLayer.gameWindow
		renderPipeline.scene = scene
	}

	/**
	 * Return the ImGui layer.
	 */
	ImGuiLayer getImGuiLayer() {

		return imGuiLayer
	}

	/**
	 * Run through all of the queued requests for the creation and deletion of
	 * graphics resources.
	 */
	void processRequests(GraphicsRenderer renderer) {

		if (deletionRequests) {
			deletionRequests.drain().each { deletionRequest ->
				def (resource, future) = deletionRequest
				renderer.delete(resource)
				future.complete(null)
			}
		}

		if (creationRequests) {
			creationRequests.drain().each { creationRequest ->
				def (request, future) = creationRequest
				var resource = switch (request) {
					case ShaderRequest -> renderer.createShader(request.shaderConfig())
					case MeshRequest -> renderer.createMesh(request.type(), request.layout(), request.vertices(), request.colour(),
						request.textureUVs(), request.dynamic(), request.index())
					case SpriteMeshRequest -> renderer.createSpriteMesh(request.surface(), request.textureUVs())
					case TextureRequest -> renderer.createTexture(request.width(), request.height(), request.format(), request.data())
					case SpriteSheetRequest -> renderer.createSpriteSheet(request.width(), request.height(), request.format(), request.data())
					case UniformBufferRequest -> renderer.createUniformBuffer(request.name(), request.data(), request.global())
					default -> throw new IllegalArgumentException("Cannot create resource from type ${request}")
				}
				future.complete(resource)
			}
		}
	}

	@Override
	<V extends GraphicsResource, R extends Request<V>> CompletableFuture<V> requestCreateOrGet(R request) {

		var future = new CompletableFuture<V>()
		creationRequests << new Tuple2(request, future)
		return future
	}

	@Override
	CompletableFuture<Void> requestDelete(GraphicsResource... resources) {

		return CompletableFuture.allOf(resources.collect { resource ->
			var future = new CompletableFuture<Void>()
			deletionRequests << new Tuple2(resource, future)
			return future
		})
	}

	@Override
	protected void runInit() {

		context = new OpenGLContext(windowTitle, config)

		window = context.window
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
			logger.debug(renderer.toString())
			EngineStats.instance.attachGraphicsRenderer(renderer)

			imGuiLayer = new ImGuiLayer(config, window)
			imGuiLayer.relay(FramebufferSizeEvent, this)

			renderPipeline = new RenderPipeline(config, window, renderer, imGuiLayer, inputEventStream)
		}
	}

	@Override
	protected void runLoop() {

		var gamepadStateProcessor = new GamepadStateProcessor(inputEventStream)

		context.withCurrent { ->
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

					// Synchronization point where we wait for the game logic
					// system to signal that it's OK to gather renderables from
					// the scene.  Once we have those renderables, we can let the
					// game logic system continue while we draw those out.
					renderBarrier.await()
					renderBarrier.reset()
					renderPipeline.gather()
					continueBarrier.await()
					continueBarrier.reset()

					renderPipeline.render()
					window.swapBuffers()
					window.pollEvents()
					gamepadStateProcessor.process()
				}
				catch (InterruptedException ignored) {
					break
				}
			}
		}
	}

	@Override
	protected void runShutdown() {

		context.withCurrent { ->
			renderPipeline.close()
			imGuiLayer.close()
			renderer.close()
		}
		context.close()
	}

	/**
	 * Called by the game logic system to signal to the graphics system that it
	 * should start gathering objects from the scene to render.  This method will
	 * block until that gathering process is complete so that it's safe for the
	 * game logic system to continue with its work and there is no longer
	 * competition for data.
	 */
	void waitForContinue() {

		renderBarrier.await()
		continueBarrier.await()
	}
}
