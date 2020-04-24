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

import nz.net.ultraq.redhorizon.engine.EngineSubsystem
import nz.net.ultraq.redhorizon.engine.KeyEvent
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import static nz.net.ultraq.redhorizon.engine.ElementLifecycleState.*
import static nz.net.ultraq.redhorizon.engine.graphics.OpenGLContext.*

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
class GraphicsEngine extends EngineSubsystem {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsEngine)

	private final boolean fixAspectRatio
	private final Closure needsMainThreadCallback
	private final List<SceneElement> sceneElements = []

	private OpenGLContext context
	private boolean started

	/**
	 * Constructor, build a new engine for rendering graphics.
	 * 
	 * @param fixAspectRatio
	 * @param needsMainThreadCallback
	 *   Closure for notifying the caller that a given method (passed as the first
	 *   parameter of the closure) needs invoking.  Some GLFW operations can only
	 *   be done on the main thread, so this indicates to the caller (which is
	 *   often the main thread) to initiate the method call.
	 */
	GraphicsEngine(boolean fixAspectRatio,
		@ClosureParams(value = SimpleType, options = 'java.util.concurrent.FutureTask') Closure needsMainThreadCallback) {

		this.fixAspectRatio = fixAspectRatio
		this.needsMainThreadCallback = needsMainThreadCallback
	}

	/**
	 * Add an element to start rendering from the next pass.
	 * 
	 * @param sceneElement
	 */
	void addSceneElement(SceneElement sceneElement) {

		sceneElements << sceneElement
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

		return !running || context.windowShouldClose()
	}

	/**
	 * Start the graphics engine loop: creates a new window in which to render the
	 * elements in the current scene, cleaning it all up when made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Graphics Engine'

		// Initialization
		context = waitForMainThread { ->
			def openGlContext = new OpenGLContext(fixAspectRatio ? ASPECT_RATIO_VGA : ASPECT_RATIO_MODERN, { key, scancode, action, mods ->
				logger.debug('Keyboard event, key: {}, scancode: {}, action: {}, mods: ', key, scancode, action, mods)
				trigger(new KeyEvent(key, scancode, action, mods))
			})
			trigger(new WindowCreatedEvent(openGlContext.windowSize))
			return openGlContext
		}
		context.withCloseable {
			OpenGLRenderer renderer
			context.withCurrent { ->
				renderer = new OpenGLRenderer(context)
			}
			def graphicsElementStates = [:]

			// Rendering loop
			logger.debug('Graphics engine in render loop...')
			started = true
			renderLoop { ->
				context.withCurrent { ->
					renderer.clear()

					sceneElements.each { sceneElement ->
						sceneElement.accept { element ->
							if (element instanceof GraphicsElement) {

								// Register the graphics element
								if (!graphicsElementStates[element]) {
									graphicsElementStates << [(element): STATE_NEW]
								}

								def elementState = graphicsElementStates[element]

								// Initialize the graphics element
								if (elementState == STATE_NEW) {
									element.init(renderer)
									elementState = STATE_INITIALIZED
									graphicsElementStates << [(element): elementState]
								}

								// Render the graphics element
								element.render(renderer)
							}
						}
					}
					context.swapBuffers()
				}
				waitForMainThread { ->
					context.withCurrent { ->
						context.pollEvents()
					}
				}
			}

			// Shutdown
			logger.debug('Shutting down graphics engine')
			context.withCurrent { ->
				graphicsElementStates.keySet().each { graphicsElement ->
					graphicsElement.delete(renderer)
				}
			}
		}
	}

	@Override
	protected boolean shouldRender() {

		return !context.windowShouldClose() && super.shouldRender()
	}

	@Override
	void stop() {

		if (running) {
			context.windowShouldClose(true)
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
