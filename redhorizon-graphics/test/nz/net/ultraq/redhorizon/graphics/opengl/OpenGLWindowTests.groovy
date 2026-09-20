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

package nz.net.ultraq.redhorizon.graphics.opengl

import nz.net.ultraq.redhorizon.graphics.Colour

import spock.lang.Specification

/**
 * Tests for the OpenGL implementation of a window.
 *
 * @author Emanuel Rabina
 */
class OpenGLWindowTests extends Specification {

	def "Opens a window"() {
		given:
			var window = new OpenGLWindow(800, 600, "Testing")
				.centerToScreen()
				.withBackgroundColour(Colour.GREY)
				.withVSync(true)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					// Do something!
				}
				Thread.yield()
			}
		then:
			noExceptionThrown()
		cleanup:
			window?.close()
	}

	def 'Cannot create a window larger than the monitor size'() {
		when:
			new OpenGLWindow(3840, 2160, 'Very large window')
				.show()
		then:
			thrown(IllegalArgumentException)
	}
}
