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
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import static nz.net.ultraq.redhorizon.engine.ElementLifecycleState.*

import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

/**
 * Graphics subsystem, creates a display which drives the rendering loop of
 * drawing graphics objects.
 * 
 * @author Emanuel Rabina
 */
class GraphicsEngine extends EngineSubsystem {

	private final SceneElement sceneElement
	private final Closure needsMainThreadCallback

	private OpenGLContext context

	/**
	 * Constructor, build a new graphics engine for rendering the given element.
	 * 
	 * @param sceneElement
	 * @param needsMainThreadCallback
	 *   Closure for notifying the caller that a given method (passed as the first
	 *   parameter of the closure) needs invoking.  Some GLFW operations can only
	 *   be done on the main thread, so this indicates to the caller (which is
	 *   often the main thread) to initiate the method call.
	 */
	GraphicsEngine(SceneElement sceneElement, Closure needsMainThreadCallback) {

		this.sceneElement = sceneElement
		this.needsMainThreadCallback = needsMainThreadCallback
	}

	/**
	 * Start the graphics engine loop: creates a new window in which to render the
	 * elements in the current scene, cleaning it all up when made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Red Horizon - Graphics Engine'

		// Initialization
		context = waitForMainThread({ ->
			return new OpenGLContext()
		})
		context.withCloseable {
			def renderer
			context.withCurrent { ->
				renderer = new OpenGLRenderer(context)
			}
			def graphicsElementStates = [:]

			// Rendering looop
			renderLoop { ->
				context.withCurrent { ->
					renderer.clear()

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
					context.swapBuffers()
				}
				waitForMainThread({ ->
					context.withCurrent { ->
						context.pollEvents()
					}
				})
			}

			// Shutdown
			context.withCurrent { ->
				graphicsElementStates.keySet().each { graphicsElement ->
					graphicsElement.delete(renderer)
				}
			}
		}
	}

	@Override
	protected boolean shouldRender() {

		return super.shouldRender() && !context.windowShouldClose()
	}

	@Override
	void stop() {

		super.stop()
		context.windowShouldClose(true)
	}

	/**
	 * Put the graphics engine in a wait state until the given task has been
	 * executed by the main thread, returning the result of execution in that
	 * thread.
	 * 
	 * @param callable
	 * @return
	 */
	private <T> T waitForMainThread(Callable<T> callable) {

		def future = new FutureTask<T>(callable)
		needsMainThreadCallback(future)
		return future.get()
	}
}
