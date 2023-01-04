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
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.EngineStoppedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLCamera
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLContext
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.InputSource
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE

/**
 * Graphics subsystem, creates a display which drives the rendering loop of
 * drawing graphics objects.
 *
 * @author Emanuel Rabina
 */
class GraphicsEngine extends Engine implements InputSource {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsEngine)

	private final String windowTitle
	private final GraphicsConfiguration config
	private final Scene scene
	private final InputEventStream inputEventStream

	private OpenGLContext context
	private OpenGLCamera camera
	private RenderPipeline renderPipeline

	private boolean shouldToggleFullScreen
	private long lastClickTime

	/**
	 * Constructor, build a new engine for rendering graphics.
	 *
	 * @param windowTitle
	 * @param config
	 * @param scene
	 * @param inputEventStream
	 */
	GraphicsEngine(String windowTitle, GraphicsConfiguration config, Scene scene, InputEventStream inputEventStream) {

		this.windowTitle = windowTitle
		this.config = config ?: new GraphicsConfiguration()
		this.scene = scene
		this.inputEventStream = inputEventStream
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
	 * Start the graphics engine loop: creates a new window in which to render the
	 * elements in the current scene, cleaning it all up when made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Graphics Engine'
		logger.debug('Starting graphics engine')

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

			context.withCurrent { ->
				trigger(new WindowCreatedEvent(window.size, window.renderResolution))

				new OpenGLRenderer(config, window).withCloseable { renderer ->
					camera = new OpenGLCamera(window.renderResolution)
					camera.withCloseable { camera ->
						new ImGuiLayer(config, window, inputEventStream).withCloseable { imGuiLayer ->
							logger.debug(renderer.toString())
							imGuiLayer.relay(FramebufferSizeEvent, this)

							renderPipeline = new RenderPipeline(config, window, renderer, imGuiLayer, inputEventStream, scene, camera)
							renderPipeline.withCloseable { pipeline ->

								// Rendering loop
								logger.debug('Graphics engine in render loop...')
								doEngineLoop(new ControlledLoop({ !window.shouldClose() }, { ->
									if (shouldToggleFullScreen) {
										window.toggleFullScreen()
										shouldToggleFullScreen = false
									}
									pipeline.render()
									window.swapBuffers()
									window.pollEvents()
								}))

								// Shutdown
								logger.debug('Shutting down graphics engine')
							}
						}
					}
				}
			}
		}
		logger.debug('Graphics engine stopped')
		trigger(new EngineStoppedEvent())
	}
}
