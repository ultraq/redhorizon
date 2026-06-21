/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.imgui.GamepadAxesOverlayModule
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler

import org.lwjgl.system.Configuration
import spock.lang.Specification

/**
 * Tests for ensuring the gamepad is reading correctly.
 *
 * @author Emanuel Rabina
 */
class GamepadProcessingCheck extends Specification {

	def setupSpec() {
		Configuration.STACK_SIZE.set(10240)
	}

	OpenGLWindow window
	OpenGLFramebuffer framebuffer
	BasicShader shader

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
			.centerToScreen()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
		framebuffer = new OpenGLFramebuffer(800, 600)
		shader = new BasicShader()
	}

	def cleanup() {
		shader?.close()
		framebuffer?.close()
		window?.close()
	}

	def 'Gamepad axes are being read'() {
		given:
			var debugOverlay = new DebugOverlay()
				.addModule(new GamepadAxesOverlayModule(window))
			var input = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
		when:
			window.show()
			while (!window.shouldClose()) {
				input.processInputs()

				window.useRenderPipeline()
					.scene { ->
						framebuffer.useFramebuffer { ->
							shader.useShader { shaderContext ->
							}
						}
					}
					.ui(false) { imGuiContext ->
						debugOverlay.render(imGuiContext)
					}
					.end()

				Thread.yield()
			}
		then:
			notThrown(Exception)
	}
}
