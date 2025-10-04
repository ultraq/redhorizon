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
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

/**
 * A simple test to see the ImGui elements in action.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class ImGuiElementsCheck extends Specification {

	def setupSpec() {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
		if (System.isMacOs()) {
			System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
		}
	}

	Scene scene
	OpenGLWindow window

	def setup() {
		scene = new Scene()
		window = new OpenGLWindow(800, 600, "Testing")
			.addFpsCounter()
			.addNodeList(scene)
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

	def 'Shows an FPS counter, node list'() {
		given:
			var node = new Node().tap {
				name = 'Parent'
			}
			scene << node
			node << new Node().tap {
				name = 'Child 1'
			}
			node << new Node().tap {
				name = 'Child 2'
			}
		when:
			window.show()
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
