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

import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Matrix4f
import org.lwjgl.system.Configuration
import org.slf4j.LoggerFactory
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * A simple test to see the ImGui elements in action.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class ImGuiElementsCheck extends Specification {

	def setupSpec() {
		Configuration.STACK_SIZE.set(10240)
	}

	OpenGLWindow window
	OpenGLFramebuffer framebuffer
	Matrix4f cameraTransform = new Matrix4f()

	def setup() {
		window = new OpenGLWindow(800, 500, "Testing")
			.centerToScreen()
			.scaleToFit()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
		framebuffer = new OpenGLFramebuffer(800, 500)
	}

	def cleanup() {
		framebuffer?.close()
		window?.close()
	}

	def 'Shows all of our panels'() {
		given:
			var logger = LoggerFactory.getLogger(ImGuiElementsCheck)
			var random = new Random()
			var scene = new Scene()
			var parent = new Node().withName('Parent')
			scene << parent
			parent
				.addChild(new Node().withName('Child 1'))
				.addChild(new Node().withName('Child 2'))

			var camera = new Camera(800, 500, window)
			var debugOverlay = new DebugOverlay()
				.withCursorTracking(camera, cameraTransform, window)
			var nodeList = new NodeList(scene)
			var logPanel = new LogPanel()
			var imGuiWindows = [debugOverlay, nodeList, logPanel]
			var input = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
			var randomLogTimer = 0f
		when:
			window.show()
			var deltaTimer = new DeltaTimer()
			while (!window.shouldClose()) {
				var delta = deltaTimer.deltaTime()
				randomLogTimer += delta
				if (randomLogTimer > 1f) {
					randomLogTimer -= 1f
					logger.info("Random log message ${random.nextInt(5)}")
				}
				input.processInputs()

				window.useRenderPipeline()
					.scene { ->
						return framebuffer
					}
					.ui(true) { imGuiContext ->
						imGuiWindows*.render(imGuiContext)
					}
					.end()
				Thread.yield()
			}
		then:
			notThrown(Exception)
	}
}
