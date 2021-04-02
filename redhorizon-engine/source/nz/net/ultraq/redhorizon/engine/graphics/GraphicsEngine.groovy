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
import nz.net.ultraq.redhorizon.engine.graphics.opengl.ImGuiRenderer
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLContext
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer
import nz.net.ultraq.redhorizon.engine.input.InputEvent
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.engine.ElementLifecycleState.*

import org.joml.FrustumIntersection
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
class GraphicsEngine extends Engine {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsEngine)

	private final GraphicsConfiguration config
	private final Closure needsMainThreadCallback

	private OpenGLContext openGlContext
	private Camera camera
	private boolean started

	private final Scene scene

	/**
	 * Constructor, build a new engine for rendering graphics.
	 * 
	 * @param scene
	 * @param config
	 * @param needsMainThreadCallback
	 *   Closure for notifying the caller that a given method (passed as the first
	 *   parameter of the closure) needs invoking.  Some GLFW operations can only
	 *   be done on the main thread, so this indicates to the caller (which is
	 *   often the main thread) to initiate the method call.
	 */
	GraphicsEngine(Scene scene, GraphicsConfiguration config,
		@ClosureParams(value = SimpleType, options = 'java.util.concurrent.FutureTask') Closure needsMainThreadCallback) {

		this.scene = scene
		this.config = config
		this.needsMainThreadCallback = needsMainThreadCallback
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

		return !running || openGlContext.windowShouldClose()
	}

	/**
	 * Start the graphics engine loop: creates a new window in which to render the
	 * elements in the current scene, cleaning it all up when made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Graphics Engine'

		// Initialization
		openGlContext = waitForMainThread { ->
			return new OpenGLContext(config, executorService)
		}
		openGlContext.withCloseable { context ->
			context.relay(InputEvent, this)
			context.relay(ContextErrorEvent, this)
			context.withCurrent { ->
				camera = new Camera(context.windowSize, config.fixAspectRatio)
				trigger(new WindowCreatedEvent(context.windowSize, camera.size), executorService)

				new OpenGLRenderer(context, config).withCloseable { renderer ->
					new ImGuiRenderer(context, renderer).withCloseable { imGuiRenderer ->
						logger.debug(renderer.toString())
						camera.init(renderer)

						def graphicsElementStates = [:]

						// Rendering loop
						logger.debug('Graphics engine in render loop...')
						started = true
						renderLoop { ->
							renderer.clear()
							imGuiRenderer.startFrame()

							camera.render(renderer)

							// Reduce the list of renderable items to those just visible in the scene
							def visibleElements = []
							def frustumIntersection = new FrustumIntersection(camera.projection * camera.view)
							averageNanos('objectCulling', 1f, logger) { ->
								scene.accept { element ->
									if (element instanceof GraphicsElement && frustumIntersection.testPlaneXY(element.bounds)) {
										visibleElements << element
									}
								}
							}
							renderer.asBatchRenderer { batchRenderer ->
								visibleElements.each { element ->

									// Register the graphics element
									if (!graphicsElementStates[element]) {
										graphicsElementStates << [(element): STATE_NEW]
									}

									def elementState = graphicsElementStates[element]

									// Initialize the graphics element
									if (elementState == STATE_NEW) {
										element.init(batchRenderer)
										elementState = STATE_INITIALIZED
										graphicsElementStates << [(element): elementState]
									}

									// Render the graphics element
									element.render(batchRenderer)
								}
								batchRenderer.flush()
							}

							imGuiRenderer.drawDebugOverlay()
							imGuiRenderer.endFrame()

							context.swapBuffers()
							waitForMainThread { ->
								context.pollEvents()
							}
						}

						// Shutdown
						logger.debug('Shutting down graphics engine')
						camera.delete(renderer)
						graphicsElementStates.keySet().each { graphicsElement ->
							graphicsElement.delete(renderer)
						}
					}
				}
			}
		}
	}

	@Override
	protected boolean shouldRender() {

		return !openGlContext.windowShouldClose() && super.shouldRender()
	}

	@Override
	void stop() {

		if (running) {
			openGlContext.windowShouldClose(true)
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
		needsMainThreadCallback(future)
		return future.get()
	}
}
