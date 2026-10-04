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

import nz.net.ultraq.redhorizon.audio.AudioListener
import nz.net.ultraq.redhorizon.audio.AudioSystem
import nz.net.ultraq.redhorizon.audio.Device
import nz.net.ultraq.redhorizon.audio.Listener
import nz.net.ultraq.redhorizon.audio.openal.OpenALDevice
import nz.net.ultraq.redhorizon.audio.openal.OpenALListener
import nz.net.ultraq.redhorizon.classic.assets.Video
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
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
 * Check that videos can be played using the {@code VideoDecoder} SPI.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class VideoDecoders extends Specification {

	def setupSpec() {
		Configuration.STACK_SIZE.set(10240)
	}

	Device device
	Listener listener
	AudioSystem audioSystem
	OpenGLWindow window
	OpenGLFramebuffer framebuffer
	BasicShader shader
	GraphicsSystem graphicsSystem
	Scene scene

	def setup() {
		device = new OpenALDevice()
		listener = new OpenALListener()
			.withGain(0.5f)
		audioSystem = new AudioSystem()

		window = new OpenGLWindow(640, 400, "Testing")
			.centerToScreen()
			.scaleToFit()
			.withBackgroundColour(Colour.BLACK)
			.withVSync(true)
			.on(KeyEvent) { event ->
				if (event.keyPressed(GLFW_KEY_ESCAPE)) {
					window.shouldClose(true)
				}
			}
		framebuffer = new OpenGLFramebuffer(640, 400)
		shader = new BasicShader()
		graphicsSystem = new GraphicsSystem(window, framebuffer, shader)
		scene = new Scene()
			.addChild(new Camera(320, 200))
			.addChild(new AudioListener())
			.addChild(new DebugOverlay())
	}

	def cleanup() {
		scene?.close()
		framebuffer?.close()
		shader?.close()
		window?.close()
		listener?.close()
		device?.close()
	}

	def 'Play a VQA file using the VideoDecoder SPI'() {
		given:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/VideoDecoders_Video_gdi1.vqa'))
			var video = new Video('VideoDecoders_Video_gdi1.vqa', inputStream)
				.scale(1f, 1.2f)
			scene.addChild(video)
		when:
			window.show()
			var deltaTimer = new DeltaTimer()
			video.play()
			while (!window.shouldClose() && video.playing) {
				var delta = deltaTimer.deltaTime()
				video.update(delta)
				audioSystem.update(scene, delta)
				graphicsSystem.update(scene, delta)
				Thread.yield()
			}
			video.stop()
		then:
			noExceptionThrown()
		cleanup:
			inputStream?.close()
	}
}
