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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

import java.nio.ByteBuffer

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
		window = new OpenGLWindow(640, 400, "Testing")
			.addFpsCounter()
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

	def "Draw a PCX file using the Image SPI"() {
		given:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/alipaper.pcx'))
			var image = new Image('alipaper.pcx', inputStream)
			var sprite = new Sprite(image)
			var shader = new BasicShader()
			var camera = new Camera(640, 400, window)
				.translate(320, 200, 0)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.update(shaderContext)
						sprite.draw(shaderContext)
					}
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
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/alipaper.cps'))
			var image = new Image('alipaper.cps', inputStream)
			var sprite = new Sprite(image)
			var shader = new BasicShader()
			var camera = new Camera(320, 200, window)
				.translate(160, 100, 0)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.update(shaderContext)
						sprite.draw(shaderContext)
					}
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

	def "Draw an SHP file using the Image SPI"() {
		given:
			var spriteSheet = getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/4tnk.shp').withBufferedStream { stream ->
				return new SpriteSheet('4tnk.shp', stream)
			}
			var sprite = new Sprite(spriteSheet)
			var faction = Faction.RED
			var adjustmentMapBuffer = ByteBuffer.allocateNative(256)
			256.times { i ->
				if (i in 80..95) {
					adjustmentMapBuffer.put(faction.colours[i - 80] as byte)
				}
				else {
					adjustmentMapBuffer.put(i as byte)
				}
			}
			adjustmentMapBuffer.flip()
			var adjustmentMap = new OpenGLTexture(256, 1, 1, adjustmentMapBuffer)
			var palette = getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/temperat.pal').withBufferedStream { stream ->
				return new Palette('temperat.pal', stream)
			}
			var alphaMaskBuffer = ByteBuffer.allocateNative(256 * 4)
			256.times { i ->
				switch (i) {
					case 0 -> alphaMaskBuffer.put(new byte[]{ 0, 0, 0, 0 })
					case 4 -> alphaMaskBuffer.put(new byte[]{ 0, 0, 0, 128 })
					default -> alphaMaskBuffer.put(new byte[]{ 255, 255, 255, 255 })
				}
			}
			alphaMaskBuffer.flip()
			var alphaMask = new OpenGLTexture(256, 1, 4, alphaMaskBuffer)
			var palettedSpriteShader = new PalettedSpriteShader()
			var camera = new Camera(320, 200, window)
				.translate(24, 24, 0)
			var timer = 0
			var frame = 0
		when:
			window.show()
			var lastUpdateTimeMs = System.currentTimeMillis()
			while (!window.shouldClose()) {
				var currentTimeMs = System.currentTimeMillis()
				var deltaMs = (currentTimeMs - lastUpdateTimeMs) / 1000 as float
				lastUpdateTimeMs = currentTimeMs

				timer += deltaMs
				if (timer > 0.25f) {
					frame = Math.wrap(frame + 1, 0, spriteSheet.numFrames)
					timer -= 0.25f
				}

				window.useWindow { ->
					palettedSpriteShader.useShader { shaderContext ->
						camera.update(shaderContext)
						shaderContext.setAdjustmentMap(adjustmentMap)
						shaderContext.setPalette(palette)
						shaderContext.setAlphaMask(alphaMask)
						sprite.draw(shaderContext, spriteSheet.getFramePosition(frame))
					}
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			palettedSpriteShader?.close()
			palette?.close()
			sprite?.close()
			spriteSheet?.close()
	}
}
