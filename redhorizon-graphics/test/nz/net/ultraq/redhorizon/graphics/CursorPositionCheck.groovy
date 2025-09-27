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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.input.KeyEvent

import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

/**
 * Testing how scaling affects things like framebuffer size and cursor position.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class CursorPositionCheck extends Specification {

	private static Logger logger = LoggerFactory.getLogger(CursorPositionCheck)

	OpenGLWindow window

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
			.centerToScreen()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
			.withFpsCounter()
			.on(KeyEvent) { event ->
				if (event.keyPressed(GLFW_KEY_ESCAPE)) {
					window.shouldClose(true)
				}
			}
	}

	def cleanup() {
		window?.close()
	}

	def 'Check cursor position'() {
		when:
			var camera = new Camera(800, 600, window)
				.translate(400, 300, 0)
			var unprojectionResult = new Vector3f()
			var inputEventHandler = new InputEventHandler()
				.addInputSource(window)
			var cursorPositionTimer = 0f
			window.show()

			var lastUpdateTimeMs = System.currentTimeMillis()

			while (!window.shouldClose()) {
				var currentTimeMs = System.currentTimeMillis()
				var delta = (currentTimeMs - lastUpdateTimeMs) / 1000 as float

				window.withFrame { ->
					// Do something!
				}

				cursorPositionTimer += delta
				var cursorPosition = inputEventHandler.cursorPosition()
				var unprojectedCursorPosition = camera.unproject(cursorPosition.x, cursorPosition.y, unprojectionResult)
				if (cursorPositionTimer > 1f) {
					logger.info('Cursor position: {}, {}', cursorPosition.x, cursorPosition.y)
					logger.info('World-projected cursor position: {}, {}', unprojectedCursorPosition.x, unprojectedCursorPosition.y)
					cursorPositionTimer = 0
				}

				lastUpdateTimeMs = currentTimeMs
				Thread.yield()
			}
		then:
			notThrown(Exception)
	}
}
