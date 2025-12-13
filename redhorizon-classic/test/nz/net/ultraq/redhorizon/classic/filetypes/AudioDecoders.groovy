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
import nz.net.ultraq.redhorizon.audio.Music
import nz.net.ultraq.redhorizon.audio.Sound
import nz.net.ultraq.redhorizon.audio.openal.OpenALAudioDevice

import org.joml.Vector3f
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * Check that a sound can be played from an AUD file using the {@link AudFileDecoder}
 * class for the {@code AudioDecoder} SPI.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class AudioDecoders extends Specification {

	def setupSpec() {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
	}

	AudioDevice device
	Vector3f position = new Vector3f()

	def setup() {
		device = new OpenALAudioDevice()
			.withMasterVolume(0.5f)
	}

	def cleanup() {
		device.close()
	}

	def "Play an AUD sound effect using the AudioDecoder SPI"() {
		when:
			var sound = getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/AudioDecoders_Sound_affirm1.v00').withBufferedStream { stream ->
				return new Sound('AudioDecoders_Sound_affirm1.v00', stream)
			}
			sound.play()
			while (!sound.stopped) {
				Thread.sleep(500)
			}
		then:
			notThrown(Exception)
		cleanup:
			sound?.close()
	}

	def "Play an AUD music track using the AudioDecoder SPI"() {
		when:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filetypes/AudioDecoders_Music_fac1226m.aud'))
			var music = new Music('AudioDecoders_Music_fac1226m.aud', inputStream)
//				.withLooping(true)
			music.play()
			var start = System.currentTimeMillis()
			while (!music.stopped) {
				music.update()
				music.render(position)
				Thread.sleep(500)
				if (System.currentTimeMillis() - start > 5000) {
					music.stop()
				}
			}
		then:
			notThrown(Exception)
		cleanup:
			music?.close()
			inputStream?.close()
	}
}
