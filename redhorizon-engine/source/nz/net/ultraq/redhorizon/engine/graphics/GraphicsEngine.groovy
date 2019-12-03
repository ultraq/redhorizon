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

/**
 * Graphics subsystem, creates a display which drives the rendering loop of
 * drawing graphics objects.
 * 
 * @author Emanuel Rabina
 */
class GraphicsEngine extends EngineSubsystem {

	private final SceneElement sceneElement

	/**
	 * Constructor, build a new graphics engine for rendering the given element.
	 * 
	 * @param sceneElement
	 */
	GraphicsEngine(SceneElement sceneElement) {

		super() { context -> !context.windowShouldClose() }
		this.sceneElement = sceneElement
	}

	/**
	 * Start the graphics engine loop: creates a new window in which to render the
	 * elements in the current scene, cleaning it all up when made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Red Horizon - Graphics Engine'

		// Initialization
		new OpenGLContext().withCloseable { context ->
			context.makeCurrent()

			def renderer = new OpenGLRenderer()
			def graphicsElementStates = [:]

			// Rendering looop
			renderLoop { ->
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
				context.pollEvents()
			}

			// Shutdown
			graphicsElementStates.keySet().each { graphicsElement ->
				graphicsElement.delete(renderer)
			}
		}

		stopLatch.countDown()
	}
}
