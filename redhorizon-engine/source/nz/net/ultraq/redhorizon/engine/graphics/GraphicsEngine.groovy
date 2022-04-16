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

import nz.net.ultraq.redhorizon.engine.ContextErrorEvent
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.EngineStoppedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLContext
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.InputSource
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
	private Camera camera
	private RenderPipeline renderPipeline

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
	 * Return the current camera.
	 * 
	 * @return
	 */
	Camera getCamera() {

		return camera
	}

	/**
	 * Return this engine's graphics context.
	 * 
	 * @return
	 */
	GraphicsContext getGraphicsContext() {

		return context
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
			context.relay(ContextErrorEvent, this)
			context.relay(FramebufferSizeEvent, this)
			context.relay(WindowMaximizedEvent, this)
			context.withCurrent { ->
				camera = new Camera(graphicsContext.renderResolution)
				trigger(new WindowCreatedEvent(context.windowSize, context.renderResolution))

				new OpenGLRenderer(config, context).withCloseable { renderer ->
					new ImGuiLayer(config, context).withCloseable { imGuiLayer ->
						logger.debug(renderer.toString())
						imGuiLayer.relay(FramebufferSizeEvent, this)
						inputEventStream.addInputSource(imGuiLayer)
						camera.init(renderer)

						renderPipeline = new RenderPipeline(config, context, renderer, imGuiLayer, scene, camera)
						renderPipeline.withCloseable { pipeline ->

							// Rendering loop
							logger.debug('Graphics engine in render loop...')
							engineLoop({ !context.windowShouldClose() }) { ->
								pipeline.render()
								context.swapBuffers()
								context.pollEvents()
							}

							// Shutdown
							logger.debug('Shutting down graphics engine')
							camera.delete(renderer)
						}
					}
				}
			}
		}
		logger.debug('Graphics engine stopped')
		trigger(new EngineStoppedEvent())
	}
}
