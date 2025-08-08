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

import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * A simple test for making sure we can create a window.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class GraphicsCheck extends Specification {

	def setupSpec() {
		System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
	}

	def "Opens a window"() {
		when:
			var window = new OpenGLWindow(800, 600, "Testing")
				.withBackgroundColour(Colour.WHITE)
				.show()
			while (!window.shouldClose()) {
				window.withFrame { ->
					// Do something!
				}
			}
		then:
			notThrown(Exception)
	}
}
