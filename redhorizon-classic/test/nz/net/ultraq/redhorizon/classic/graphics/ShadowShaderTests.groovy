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

package nz.net.ultraq.redhorizon.classic.graphics

import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

/**
 * Tests for the shadow shader.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class ShadowShaderTests extends Specification {

	def setupSpec() {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
		if (System.isMacOs()) {
			System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
		}
	}

	OpenGLWindow window

	def setup() {
		window = new OpenGLWindow(640, 400, "Testing")
			.addDebugOverlay(new DebugOverlay())
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

	def 'Draws a silhouette of a sprite'() {
		given:
			var spriteSheet = getResourceAsStream('nz/net/ultraq/redhorizon/classic/graphics/ShadowShaderTests_Shadow_mig.shp').withBufferedStream { stream ->
				return new SpriteSheet('ShadowShaderTests_Shadow_mig.shp', stream)
			}
			var sprite = new Sprite(spriteSheet)
			var shadowShader = new ShadowShader()
			var camera = new Camera(320, 200, window)
				.translate(24, 24, 0)
			var timer = 0
			var frame = 0
		when:
			window.show()
			var lastUpdateTimeMs = System.currentTimeMillis()
			while (!window.shouldClose()) {
				var currentTimeMs = System.currentTimeMillis()
				var delta = (currentTimeMs - lastUpdateTimeMs) / 1000 as float
				lastUpdateTimeMs = currentTimeMs

				timer += delta
				if (timer > 0.25f) {
					frame = Math.wrap(frame + 1, 0, spriteSheet.numFrames)
					timer -= 0.25f
				}

				window.useWindow { ->
					shadowShader.useShader { shaderContext ->
						camera.update(shaderContext)
						sprite.draw(shaderContext, spriteSheet.getFramePosition(frame))
					}
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			shadowShader?.close()
			sprite?.close()
			spriteSheet?.close()
	}
}
