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
import nz.net.ultraq.redhorizon.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.*

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

	def 'Shows a debug overlay, node list'() {
		given:
			var scene = new Scene()
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
			var camera = new Camera(800, 600, window)
			var eventHandler = new InputEventHandler()
				.addInputSource(window)
		when:
			window
				.addDebugOverlay(new DebugOverlay()
					.withCursorTracking(camera))
				.addNodeList(new NodeList(scene))
				.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					// Do something!

					if (eventHandler.keyPressed(GLFW_KEY_V, true)) {
						window.toggleVSync()
					}
					if (eventHandler.keyPressed(GLFW_KEY_I, true)) {
						window.toggleImGuiWindows()
					}
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
	}
}
