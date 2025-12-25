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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Matrix4f
import org.lwjgl.system.Configuration
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
		Configuration.STACK_SIZE.set(10240)
		if (System.isMacOs()) {
			Configuration.GLFW_LIBRARY_NAME.set('glfw_async')
		}
	}

	OpenGLWindow window
	Matrix4f cameraTransform = new Matrix4f()

	def setup() {
		window = new OpenGLWindow(800, 500, "Testing")
			.centerToScreen()
			.scaleToFit()
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
			var node = new Node().withName('Parent')
			scene << node
			node << new Node().withName('Child 1')
			node << new Node().withName('Child 2')
			var camera = new Camera(800, 500, window)
			var eventHandler = new InputEventHandler()
				.addInputSource(window)
		when:
			window
				.addImGuiComponent(new DebugOverlay()
					.withCursorTracking(camera, cameraTransform))
				.addImGuiComponent(new NodeList(scene))
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
