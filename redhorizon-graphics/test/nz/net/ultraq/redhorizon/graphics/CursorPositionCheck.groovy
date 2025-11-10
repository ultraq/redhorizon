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

import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

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

	OpenGLWindow window

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
			.centerToScreen()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
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
			window
				.addDebugOverlay(new DebugOverlay()
					.withCursorTracking(camera))
				.show()

			while (!window.shouldClose()) {
				window.useWindow { ->
					// Do something!
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
	}
}
