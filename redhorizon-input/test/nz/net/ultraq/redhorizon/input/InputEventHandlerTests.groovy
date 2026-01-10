/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.input

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * Tests for the {@link InputEventHandler}.
 *
 * @author Emanuel Rabina
 */
class InputEventHandlerTests extends Specification {

	static class TestInputSource implements InputSource {}

	static final int KEY_A = 65
	static final int MOUSE_BUTTON_1 = 0

	def "Key is considered 'pressed' after the key press event"() {
		given:
			var inputSource = new TestInputSource()
			var eventHandler = new InputEventHandler()
				.addInputSource(inputSource)
		when:
			inputSource.trigger(new KeyEvent(KEY_A, 0, true, false))
		then:
			new PollingConditions().eventually { ->
				assert eventHandler.keyPressed(KEY_A)
			}
	}

	def "Key is considered 'released' when a key release event follows"() {
		given:
			var inputSource = new TestInputSource()
			var eventHandler = new InputEventHandler()
				.addInputSource(inputSource)
		when:
			inputSource.trigger(new KeyEvent(KEY_A, 0, true, false))
			inputSource.trigger(new KeyEvent(KEY_A, 0, false, false))
		then:
			new PollingConditions().eventually { ->
				assert !eventHandler.keyPressed(KEY_A)
			}
	}

	def "Key is considered pressed just once when 'once' is specified"() {
		given:
			var inputSource = new TestInputSource()
			var eventHandler = new InputEventHandler()
				.addInputSource(inputSource)
		when:
			inputSource.trigger(new KeyEvent(KEY_A, 0, true, false))
		then:
			new PollingConditions().eventually { ->
				assert eventHandler.keyPressed(KEY_A, true)
			}
			!eventHandler.keyPressed(KEY_A)
	}

	def "Mouse button is considered 'pressed' after the mouse button press event"() {
		given:
			var inputSource = new TestInputSource()
			var eventHandler = new InputEventHandler()
				.addInputSource(inputSource)
		when:
			inputSource.trigger(new MouseButtonEvent(MOUSE_BUTTON_1, 0, true))
		then:
			new PollingConditions().eventually { ->
				assert eventHandler.mouseButtonPressed(MOUSE_BUTTON_1)
			}
	}

	def "Mouse button is considered 'released' after the mouse button release event"() {
		given:
			var inputSource = new TestInputSource()
			var eventHandler = new InputEventHandler()
				.addInputSource(inputSource)
		when:
			inputSource.trigger(new MouseButtonEvent(MOUSE_BUTTON_1, 0, true))
			inputSource.trigger(new MouseButtonEvent(MOUSE_BUTTON_1, 0, false))
		then:
			new PollingConditions().eventually { ->
				assert !eventHandler.mouseButtonPressed(MOUSE_BUTTON_1)
			}
	}

	def "Tracks cursor position through events"() {
		given:
			var inputSource = new TestInputSource()
			var eventHandler = new InputEventHandler()
				.addInputSource(inputSource)
		when:
			inputSource.trigger(new CursorPositionEvent(100, 50))
		then:
			new PollingConditions().eventually { ->
				var cursorPosition = eventHandler.cursorPosition()
				assert cursorPosition.x() == 100 && cursorPosition.y() == 50
			}
	}
}
