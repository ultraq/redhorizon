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

import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

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

	def "Ensure sprites don't share the same frame when rendered together"() {
		when:
			var shader = new BasicShader()
			var image = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/SpriteTests_SpriteSheet.png').withBufferedStream { stream ->
				return new SpriteSheet('SpriteTests_SpriteSheet.png', 32, 32, stream)
			}
			var sprite1 = new Sprite(image, BasicShader)
				.translate(-16f, 0f)
			var sprite2 = new Sprite(image, BasicShader)
				.translate(16f, 0f)
			var camera = new Camera(80, 60)
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.render(shaderContext)
						sprite1.withFramePosition(16)
						sprite2.withFramePosition(18)
						sprite1.render(shaderContext)
						sprite2.render(shaderContext)
					}
				}
				Thread.yield()
			}
		then:
			noExceptionThrown()
		cleanup:
			sprite1?.close()
			sprite2?.close()
			image?.close()
			shader?.close()
	}
}
