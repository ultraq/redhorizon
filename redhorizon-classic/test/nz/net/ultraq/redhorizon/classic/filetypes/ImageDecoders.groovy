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
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.PaletteAlphaMask
import nz.net.ultraq.redhorizon.graphics.PaletteSwapMap
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.Runtime
import nz.net.ultraq.redhorizon.runtime.objects.GridLines
import nz.net.ultraq.redhorizon.scene.Node
import nz.net.ultraq.redhorizon.scene.Scene
import nz.net.ultraq.redhorizon.script.Script
import nz.net.ultraq.redhorizon.script.ScriptNode
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.ASSET_MANAGER

import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F

/**
 * Check that images can be read and rendered using the {@code ImageDecoder}
 * SPI.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class ImageDecoders extends Specification {

	private class ImageDecoderApplication extends Application {
		ImageDecoderApplication() {
			super('Image Decoder', 'test')
		}
	}

	def "Draw a PCX file using the Image SPI"() {
		when:
			new Runtime(new ImageDecoderApplication() {
				@Override
				protected Scene configureScene(Scene scene) {
					var assetManager = ASSET_MANAGER.get()
					var image = assetManager.loadImage('ImageDecoders_Image_alipaper.pcx')
					return scene.addChild(new Sprite(image, BasicShader))
				}
			})
				.withWindowWidth(640)
				.withWindowHeight(400)
				.execute()
		then:
			noExceptionThrown()
	}

	def "Draw a CPS file using the Image SPI"() {
		when:
			new Runtime(new ImageDecoderApplication() {
				@Override
				protected Scene configureScene(Scene scene) {
					var assetManager = ASSET_MANAGER.get()
					var image = assetManager.loadImage('ImageDecoders_Image_alipaper.cps')
					return scene.addChild(new Sprite(image, BasicShader))
				}
			})
				.withWindowWidth(320)
				.withWindowHeight(200)
				.execute()
		then:
			noExceptionThrown()
	}

	static class ShpFileScript extends Script<Node> {

		private Faction faction = Faction.RED
		private float timer = 0f
		private int frame = 0

		@Override
		void update(float delta) {

			if (input.keyPressed(GLFW_KEY_F, true)) {
				faction++
				node.find(PaletteSwapMap).setColourIndexes(faction.colourIndexes)
			}

			timer += delta
			if (timer > 0.1f) {
				frame = (frame + 1) % 64
				node.find(Sprite).withFramePosition(frame)
				timer -= 0.1f
			}
		}
	}

	def "Draw an SHP file using the Image SPI"() {
		when:
			new Runtime(new ImageDecoderApplication() {
				@Override
				protected Scene configureScene(Scene scene) {
					var assetManager = ASSET_MANAGER.get()
					var spriteSheet = assetManager.loadSpriteSheet('ImageDecoders_SpriteSheet_4tnk.shp')
					var palette = assetManager.loadPalette('Palette_temperat.pal')
					return scene.addChild(
						new Node()
							.withName('Mammoth Tank')
							.addChild(palette)
							.addChild(new PaletteAlphaMask())
							.addChild(new PaletteSwapMap(Faction.RED.colourIndexes))
							.addChild(new ScriptNode(ShpFileScript))
							.addChild(new Sprite(spriteSheet, PalettedSpriteShader))
					)
				}
			})
				.withWindowWidth(640)
				.withWindowHeight(400)
				.withAdditionalShaders { ->
					return new PalettedSpriteShader()
				}
				.execute()
		then:
			noExceptionThrown()
	}

	static class WsaFileScript extends Script<Animation> {

		private float timer = 0f
		private int frame = 0

		@Override
		void init() {

			node.scene.find(GridLines).disable()
			node.play()
		}

		@Override
		void update(float delta) {

			node.update(delta)
		}
	}

	def "Play a WSA file using the Image SPI"() {
		when:
			new Runtime(new ImageDecoderApplication() {
				@Override
				protected Scene configureScene(Scene scene) {
					// TODO: Add extension method for loadAnimation?
					var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/ImageDecoders_Animation_africa.wsa'))
					return scene.addChild(
						new Animation('ImageDecoders_Animation_africa.wsa', inputStream)
							.scale(1.6625f, 2f)
							.addChild(new ScriptNode(WsaFileScript))
					)
				}
			})
				.withWindowWidth(640)
				.withWindowHeight(400)
				.withWindowBackgroundColour(Colour.BLACK)
				.execute()
		then:
			noExceptionThrown()
	}
}
