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

import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.actions.CloseWindowAction
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.scene.Scene
import nz.net.ultraq.redhorizon.time.DeltaTimer

import org.joml.Vector3f
import org.lwjgl.system.Configuration
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

/**
 * A simple test for making sure we can render objects using the graphics
 * module.
 *
 * <p>The image file used for testing is {@code ship_0000.png} from
 * <a href="https://kenney.nl/assets/pixel-shmup">Kenney's Pixel Shmup</a>.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class GraphicsCheck extends Specification {

	def setupSpec() {
		Configuration.STACK_SIZE.set(10240)
	}

	Window window
	Framebuffer framebuffer
	BasicShader shader
	GraphicsSystem graphicsSystem
	Scene scene
	DeltaTimer timer = new DeltaTimer()

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
			.centerToScreen()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
			.on(KeyEvent) { event ->
				if (event.keyPressed(GLFW_KEY_ESCAPE)) {
					new CloseWindowAction(window).execute()
				}
			}
		framebuffer = new OpenGLFramebuffer(1600, 1200)
		shader = new BasicShader()
		graphicsSystem = new GraphicsSystem(window, framebuffer, shader)
		scene = new Scene()
			.addChild(new Camera(800, 600))
	}

	def cleanup() {
		scene?.close()
		shader?.close()
		framebuffer?.close()
		window?.close()
	}

	def "Opens a window with the debug overlay"() {
		given:
			scene.addChild(new DebugOverlay())
		when:
			window.show()
			while (!window.shouldClose()) {
				graphicsSystem.update(scene, timer.deltaTime())
				Thread.yield()
			}
		then:
			noExceptionThrown()
	}

	def "Draws a triangle"() {
		given:
			scene.addChild(new Shape(Type.TRIANGLES, new Vertex[]{
				new Vertex(new Vector3f(0, 150, 0), Colour.RED),
				new Vertex(new Vector3f(-200, -150, 0), Colour.GREEN),
				new Vertex(new Vector3f(200, -150, 0), Colour.BLUE)
			}))
		when:
			window.show()
			while (!window.shouldClose()) {
				graphicsSystem.update(scene, timer.deltaTime())
				Thread.yield()
			}
		then:
			noExceptionThrown()
	}
}
