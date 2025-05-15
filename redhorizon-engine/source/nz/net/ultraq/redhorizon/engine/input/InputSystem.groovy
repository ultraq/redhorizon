/*
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.input

import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.EngineSystemType
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

import java.util.concurrent.BlockingQueue
import java.util.concurrent.BrokenBarrierException
import java.util.concurrent.LinkedBlockingQueue

/**
 * The input system for accepting input from one or more sources and processing
 * them during the input stage of the game loop.
 *
 * @author Emanuel Rabina
 */
class InputSystem extends EngineSystem implements InputRequests, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(InputSystem)

	final EngineSystemType type = EngineSystemType.INPUT

	private final BlockingQueue<InputEvent> inputEvents = new LinkedBlockingQueue<>()

	@Override
	RemoveControlFunction addControl(Control control) {

		var removeEventFunction = on(control.event, control)
		trigger(new ControlAddedEvent(control))

		return { ->
			removeEventFunction.remove()
			trigger(new ControlRemovedEvent(control))
		}
	}

	/**
	 * Add a source for input events that can be listened to using this object.
	 */
	void addInputSource(InputSource inputSource) {

		inputSource.on(InputEvent) { event ->
			if (event instanceof KeyEvent && event.action == GLFW_PRESS) {
				logger.trace("Key: {}, mods: {}", event.key, event.mods)
			}
			inputEvents << event
		}
	}

	@Override
	void configureScene() {

		scene.inputRequestHandler = this
	}

	@Override
	protected void runInit() {

		var graphicsSystem = engine.findSystem(GraphicsSystem)
		graphicsSystem.on(WindowCreatedEvent) { event ->
			addInputSource(event.window)
		}
	}

	@Override
	protected void runLoop() {

		List<InputEvent> localInputsEvents = []
		List<InputHandler> inputHandlers = []

		try {
			while (!Thread.interrupted()) {
				process { ->
					localInputsEvents.clear()
					inputHandlers.clear()

					if (inputEvents) {
						inputEvents.drainTo(localInputsEvents)

						// Process inputs through handlers in the scene
						if (scene?.query(InputHandler, inputHandlers)) {
							var inputEventsIterator = localInputsEvents.listIterator()
							while (inputEventsIterator.hasNext()) {
								var inputEvent = inputEventsIterator.next()
								var inputHandled = inputHandlers.any { inputHandler ->
									return inputHandler.input(inputEvent)
								}
								if (inputHandled) {
									inputEventsIterator.remove()
								}
							}
						}

						// Refire any unhandled inputs as events.  This should be replaced
						// with something else like the above, but for a way for non-scene
						// objects to participate 🤔
						localInputsEvents.each { inputEvent ->
							trigger(inputEvent)
						}
					}
				}
			}
		}
		catch (InterruptedException | BrokenBarrierException ignored) {
			// Do nothing
		}
	}
}
