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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.actions.CloseWindowAction
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.scene.Scene
import nz.net.ultraq.redhorizon.time.DeltaTimer

import org.lwjgl.system.Configuration
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

/**
 * Tests for sprite-related aspects.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class SpriteTests extends Specification {

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
			.addChild(new Camera(80, 60))
	}

	def cleanup() {
		scene?.close()
		shader?.close()
		framebuffer?.close()
		window?.close()
	}

	def "Draws a sprite - using Image and ImageDecoder SPI"() {
		given:
			var spriteSheet = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/SpriteTests_Image.png').withBufferedStream { stream ->
				return new Image('SpriteTests_Image.png', stream)
			}
			scene.addChild(new Sprite(spriteSheet, BasicShader))
		when:
			window.show()
			while (!window.shouldClose()) {
				graphicsSystem.update(scene, timer.deltaTime())
				Thread.yield()
			}
		then:
			noExceptionThrown()
		cleanup:
			spriteSheet?.close()
	}

	def "Ensure sprites don't share the same frame when rendered together"() {
		when:
			var spriteSheet = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/SpriteTests_SpriteSheet.png').withBufferedStream { stream ->
				return new SpriteSheet('SpriteTests_SpriteSheet.png', 32, 32, stream)
			}
			scene
				.addChild(new Sprite(spriteSheet, BasicShader)
					.withFramePosition(16)
					.translate(-16f, 0f))
				.addChild(new Sprite(spriteSheet, BasicShader)
					.withFramePosition(18)
					.translate(16f, 0f))
			window.show()
			while (!window.shouldClose()) {
				graphicsSystem.update(scene, timer.deltaTime())
				Thread.yield()
			}
		then:
			noExceptionThrown()
		cleanup:
			spriteSheet?.close()
	}
}
