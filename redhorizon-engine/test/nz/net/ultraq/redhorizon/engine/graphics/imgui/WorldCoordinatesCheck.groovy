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

import nz.net.ultraq.redhorizon.engine.DeltaTimer
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Vertex
import nz.net.ultraq.redhorizon.graphics.imgui.CursorTrackingOverlayModule
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler

import org.joml.Vector3f
import org.lwjgl.system.Configuration
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.*

/**
 * A test for ensuring that we can map between window and world coordinates.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class WorldCoordinatesCheck extends Specification {

	def setupSpec() {
		Configuration.STACK_SIZE.set(10240)
	}

	OpenGLWindow window
	OpenGLFramebuffer framebuffer
	BasicShader shader

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
			.centerToScreen()
			.scaleToFit()
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

	def 'Convert window coordinates to world coordinates'() {
		given:
			var camera = new Camera(800, 600)
			var originLines = new OpenGLMesh(Type.LINES, new Vertex[]{
				new Vertex(new Vector3f(-300, 0, 0), Colour.RED),
				new Vertex(new Vector3f(300, 0, 0), Colour.RED),
				new Vertex(new Vector3f(0, -300, 0), Colour.RED),
				new Vertex(new Vector3f(0, 300, 0), Colour.RED)
			})
			var debugOverlay = new DebugOverlay()
				.addModule(new CursorTrackingOverlayModule(window, camera))
			var input = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
		when:
			window.show()
			var deltaTimer = new DeltaTimer()
			while (!window.shouldClose()) {
				var delta = deltaTimer.deltaTime()

				input.processInputs()
				if (input.keyPressed(GLFW_KEY_A)) {
					camera.translate(-100f * delta as float, 0f)
				}
				if (input.keyPressed(GLFW_KEY_D)) {
					camera.translate(100f * delta as float, 0f)
				}
				if (input.keyPressed(GLFW_KEY_W)) {
					camera.translate(0f, 100f * delta as float)
				}
				if (input.keyPressed(GLFW_KEY_S)) {
					camera.translate(0f, -100f * delta as float)
				}
				if (input.keyPressed(GLFW_KEY_SPACE)) {
					camera.resetTransform()
				}

				window.useRenderPipeline()
					.scene { ->
						framebuffer.useFramebuffer { ->
							shader.useShader { shaderContext ->
								camera.render(shaderContext)
								originLines.render(shaderContext)
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
			noExceptionThrown()
		cleanup:
			originLines?.close()
	}
}
