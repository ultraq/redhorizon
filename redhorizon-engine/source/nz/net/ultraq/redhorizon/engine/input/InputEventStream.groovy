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

import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

/**
 * The input event stream for relaying input events from other input sources.
 * 
 * @author Emanuel Rabina
 */
class InputEventStream implements EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(InputEventStream)

	/**
	 * Constructor, set the source for input events that will end up on the input
	 * queue and actioned at set intervals.
	 * 
	 * @param inputSource
	 */
	InputEventStream(InputSource inputSource) {

		inputSource.on(InputEvent) { event ->
			if (event instanceof KeyEvent && event.action == GLFW_PRESS) {
				logger.info("Key: {}, mods: {}", event.key, event.mods)
			}
			trigger(event)
		}
	}
}
