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

package nz.net.ultraq.redhorizon.classic.filedecoders

import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.*

/**
 * Check that a PCX file can be read and rendered using the {@link PcxFileDecoder}
 * class for the {@code ImageDecoder} SPI.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class ImageDecoderTests extends Specification {

	def setupSpec() {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
		if (System.isMacOs()) {
			System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
		}
	}

	OpenGLWindow window

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing", true)
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

	def "Draw a PCX file using the Image SPI"() {
		given:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filedecoders/alipaper.pcx'))
			var image = new Image('alipaper.pcx', inputStream)
			var sprite = new Sprite(image)
			var shader = new BasicShader()
			var camera = new Camera(640, 480)
				.attachWindow(window)
			camera.view.translate(-320, -200, 0)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.withFrame { ->
					shader.use()
					camera.update(shader)
					sprite.draw(shader)
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			shader?.close()
			sprite?.close()
			image?.close()
			inputStream?.close()
	}

	def "Draw a CPS file using the Image SPI"() {
		given:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filedecoders/alipaper.cps'))
			var image = new Image('alipaper.cps', inputStream)
			var sprite = new Sprite(image)
			var shader = new BasicShader()
			var camera = new Camera(320, 240)
				.attachWindow(window)
			camera.view.translate(-160, -100, 0)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.withFrame { ->
					shader.use()
					camera.update(shader)
					sprite.draw(shader)
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			shader?.close()
			sprite?.close()
			image?.close()
			inputStream?.close()
	}
}
