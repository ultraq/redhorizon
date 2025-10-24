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

import nz.net.ultraq.redhorizon.audio.AudioDevice
import nz.net.ultraq.redhorizon.audio.openal.OpenALAudioDevice
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Video
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

/**
 * Check that videos can be played using the {@code VideoDecoder} SPI.
 *
 * @author Emanuel Rabina
 */
class VideoDecoderTests extends Specification {

	def setupSpec() {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
		if (System.isMacOs()) {
			System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
		}
	}

	AudioDevice device
	OpenGLWindow window

	def setup() {
		device = new OpenALAudioDevice()
			.withMasterVolume(0.5f)
		window = new OpenGLWindow(640, 400, "Testing")
			.addFpsCounter()
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
		device?.close()
		window?.close()
	}

	def 'Play a VQA file using the VideoDecoder SPI'() {
		given:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/samdie.vqa'))
			var video = new Video('samdie.vqa', inputStream, 320, 188)
			var shader = new BasicShader()
			var camera = new Camera(320, 200, window)
				.translate(160, 94, 0)
		when:
			window.show()
			video.play()
			var lastUpdateTimeMs = System.currentTimeMillis()
			while (!window.shouldClose() && video.playing) {
				var currentTimeMs = System.currentTimeMillis()
				var delta = (currentTimeMs - lastUpdateTimeMs) / 1000 as float
				lastUpdateTimeMs = currentTimeMs

				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.update(shaderContext)
						video.update(delta)
						video.draw(shaderContext)
					}
				}
				Thread.yield()
			}
			video.stop()
		then:
			notThrown(Exception)
		cleanup:
			shader?.close()
			video?.close()
			inputStream?.close()
	}
}
