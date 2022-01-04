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
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLContext
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.InputSource
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.FutureTask

/**
 * Graphics subsystem, creates a display which drives the rendering loop of
 * drawing graphics objects.
 * 
 * @author Emanuel Rabina
 */
class GraphicsEngine extends Engine implements InputSource {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsEngine)

	private final GraphicsConfiguration config
	private final Scene scene
	private final InputEventStream inputEventStream
	private final Closure mainThreadCallback

	private GraphicsContext graphicsContext
	private Camera camera
	private RenderPipeline renderPipeline
	private boolean started

	/**
	 * Constructor, build a new engine for rendering graphics.
	 * 
	 * @param config
	 * @param scene
	 * @param inputEventStream
	 * @param mainThreadCallback
	 *   Closure for notifying the caller that a given method (passed as the first
	 *   parameter of the closure) needs invoking.  Some GLFW operations can only
	 *   be done on the main thread, so this indicates to the caller (which is
	 *   often the main thread) to initiate the method call.
	 */
	GraphicsEngine(GraphicsConfiguration config, Scene scene, InputEventStream inputEventStream,
		@ClosureParams(value = SimpleType, options = 'java.util.concurrent.FutureTask') Closure mainThreadCallback) {

		this.config = config ?: new GraphicsConfiguration()
		this.scene = scene
		this.inputEventStream = inputEventStream
		this.mainThreadCallback = System.getProperty('os.name').contains('Mac OS') ?
			mainThreadCallback :
			{ FutureTask<?> executable -> executable.run() }
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

		return graphicsContext
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
	 * Return whether or not the graphics engine has been started.
	 * 
	 * @return
	 */
	boolean isStarted() {

		return started
	}

	/**
	 * Return whether or not the graphics engine has been stopped.
	 * 
	 * @return
	 */
	boolean isStopped() {

		return !running || graphicsContext.windowShouldClose()
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
		graphicsContext = waitForMainThread { ->
			return new OpenGLContext(config)
		}
		graphicsContext.withCloseable { context ->
			context.relay(ContextErrorEvent, this)
			context.relay(WindowMaximizedEvent, this)
			context.withCurrent { ->
				camera = new Camera(context.renderResolution)
				trigger(new WindowCreatedEvent(context.windowSize, context.renderResolution))

				new OpenGLRenderer(config, context).withCloseable { renderer ->
					new ImGuiLayer(config, context).withCloseable { imGuiLayer ->
						logger.debug(renderer.toString())
						inputEventStream.addInputSource(imGuiLayer)
						camera.init(renderer)

						renderPipeline = new RenderPipeline(config, context, renderer, imGuiLayer, scene, camera)
						renderPipeline.withCloseable { pipeline ->

							// Rendering loop
							logger.debug('Graphics engine in render loop...')
							started = true
							engineLoop { ->
								pipeline.render()
								context.swapBuffers()
								waitForMainThread { ->
									context.pollEvents()
								}
							}

							// Shutdown
							logger.debug('Shutting down graphics engine')
							camera.delete(renderer)
						}
					}
				}
			}
		}
	}

	@Override
	protected boolean shouldRun() {

		return !graphicsContext.windowShouldClose() && super.shouldRun()
	}

	@Override
	void stop() {

		if (running) {
			graphicsContext.windowShouldClose(true)
		}
		super.stop()
	}

	/**
	 * Put the graphics engine in a wait state until the given task has been
	 * executed by the main thread, returning the result of execution in that
	 * thread.
	 * 
	 * @param closure
	 * @return
	 */
	private <T> T waitForMainThread(Closure<T> closure) {

		def future = new FutureTask<T>(closure)
		mainThreadCallback(future)
		return future.get()
	}
}
