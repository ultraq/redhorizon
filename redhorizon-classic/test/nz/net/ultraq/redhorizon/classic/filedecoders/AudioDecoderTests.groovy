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

import nz.net.ultraq.redhorizon.audio.AudioDevice
import nz.net.ultraq.redhorizon.audio.Music
import nz.net.ultraq.redhorizon.audio.Sound
import nz.net.ultraq.redhorizon.audio.openal.OpenALAudioDevice

import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * Check that a sound can be played from an AUD file using the {@link AudFileDecoder}
 * class for the {@code AudioDecoder} SPI.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class AudioDecoderTests extends Specification {

	def setupSpec() {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
	}

	AudioDevice device

	def setup() {
		device = new OpenALAudioDevice()
			.withMasterVolume(0.5f)
	}

	def cleanup() {
		device.close()
	}

	def "Play an AUD sound effect using the AudioDecoder SPI"() {
		when:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filedecoders/affirm1.v00'))
			var sound = new Sound('affirm1.v00', inputStream)
			sound.play()
			while (sound.playing) {
				Thread.sleep(100)
			}
		then:
			notThrown(Exception)
		cleanup:
			sound?.close()
			inputStream?.close()
	}

	def "Play an AUD music track using the AudioDecoder SPI"() {
		when:
			var inputStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/classic/filedecoders/fac1226m.aud'))
			var music = new Music('fac1226m.aud', inputStream)
			music.play()
			while (music.playing) {
				music.update()
				Thread.sleep(100)
			}
		then:
			notThrown(Exception)
		cleanup:
			music?.close()
			inputStream?.close()
	}
}
