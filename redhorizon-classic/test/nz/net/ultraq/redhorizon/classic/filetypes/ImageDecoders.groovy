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
import nz.net.ultraq.redhorizon.classic.graphics.AlphaMask
import nz.net.ultraq.redhorizon.classic.graphics.FactionAdjustmentMap
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

import org.joml.Matrix4f
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.*

/**
 * Check that images can be read and rendered using the {@code ImageDecoder}
 * SPI.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class ImageDecoders extends Specification {

	def setupSpec() {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
		if (System.isMacOs()) {
			System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
		}
	}

	OpenGLWindow window
	Matrix4f transform = new Matrix4f()

	def setup() {
		window = new OpenGLWindow(640, 400, "Testing")
			.addImGuiComponent(new DebugOverlay())
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

	def "Draw a PCX file using the Image SPI"() {
		given:
			var image = getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/ImageDecoders_Image_alipaper.pcx').withBufferedStream { stream ->
				return new Image('ImageDecoders_Image_alipaper.pcx', stream)
			}
			var sprite = new Sprite(image)
			var shader = new BasicShader()
			var camera = new Camera(640, 400, window)
				.translate(320, 200, 0)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.render(shaderContext)
						sprite.render(shaderContext, transform)
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
	}

	def "Draw a CPS file using the Image SPI"() {
		given:
			var image = getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/ImageDecoders_Image_alipaper.cps').withBufferedStream { stream ->
				return new Image('ImageDecoders_Image_alipaper.cps', stream)
			}
			var sprite = new Sprite(image)
			var shader = new BasicShader()
			var camera = new Camera(320, 200, window)
				.translate(160, 100, 0)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.render(shaderContext)
						sprite.render(shaderContext, transform)
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
	}

	def "Draw an SHP file using the Image SPI"() {
		given:
			var spriteSheet = getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/ImageDecoders_SpriteSheet_4tnk.shp').withBufferedStream { stream ->
				return new SpriteSheet('ImageDecoders_SpriteSheet_4tnk.shp', stream)
			}
			var sprite = new Sprite(spriteSheet)
			var faction = Faction.RED
			var adjustmentMap = new FactionAdjustmentMap(faction)
			var palette = getResourceAsStream('nz/net/ultraq/redhorizon/classic/Palette_temperat.pal').withBufferedStream { stream ->
				return new Palette('Palette_temperat.pal', stream)
			}
			var alphaMask = new AlphaMask()
			var palettedSpriteShader = new PalettedSpriteShader()
			var camera = new Camera(320, 200, window)
				.translate(24, 24, 0)
			var timer = 0
			var frame = 0
			window.on(KeyEvent) { event ->
				if (event.keyPressed(GLFW_KEY_P)) {
					var factions = Faction.values()
					faction = factions[Math.wrap(faction.ordinal() + 1, 0, factions.length)]
					adjustmentMap.setFaction(faction)
				}
			}
		when:
			window.show()
			var lastUpdateTimeMs = System.currentTimeMillis()
			while (!window.shouldClose()) {
				var currentTimeMs = System.currentTimeMillis()
				var delta = (currentTimeMs - lastUpdateTimeMs) / 1000 as float
				lastUpdateTimeMs = currentTimeMs

				timer += delta
				if (timer > 0.1f) {
					frame = Math.wrap(frame + 1, 0, spriteSheet.numFrames)
					timer -= 0.1f
				}

				window.useWindow { ->
					palettedSpriteShader.useShader { shaderContext ->
						camera.render(shaderContext)
						shaderContext.setAdjustmentMap(adjustmentMap)
						adjustmentMap.update()
						shaderContext.setPalette(palette)
						shaderContext.setAlphaMask(alphaMask)
						sprite.render(shaderContext, transform, spriteSheet.getFramePosition(frame))
					}
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			alphaMask?.close()
			adjustmentMap?.close()
			palettedSpriteShader?.close()
			palette?.close()
			sprite?.close()
			spriteSheet?.close()
	}

	def "Play a WSA file using the Image SPI"() {
		given:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/ImageDecoders_Animation_africa.wsa'))
			var animation = new Animation('ImageDecoders_Animation_africa.wsa', inputStream, 266, 200)
			var shader = new BasicShader()
			var camera = new Camera(320, 200, window)
				.translate(133, 100, 0)
		when:
			window.show()
			animation.play()
			var lastUpdateTimeMs = System.currentTimeMillis()
			while (!window.shouldClose() && animation.playing) {
				var currentTimeMs = System.currentTimeMillis()
				var delta = (currentTimeMs - lastUpdateTimeMs) / 1000 as float
				lastUpdateTimeMs = currentTimeMs

				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.render(shaderContext)
						animation.update(delta)
						animation.render(shaderContext, transform)
					}
				}
				Thread.yield()
			}
			animation.stop()
		then:
			notThrown(Exception)
		cleanup:
			shader?.close()
			animation?.close()
			inputStream?.close()
	}
}
