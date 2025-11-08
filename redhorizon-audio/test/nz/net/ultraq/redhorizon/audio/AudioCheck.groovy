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

package nz.net.ultraq.redhorizon.audio

import nz.net.ultraq.redhorizon.audio.openal.OpenALAudioDevice

import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * A simple class to play back a sound using the audio module.
 *
 * <p>The audio file used for testing is {@code bong_001.ogg} from
 * <a href="https://kenney.nl/assets/interface-sounds">Kenney's Interface Sounds</a>.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class AudioCheck extends Specification {

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

	def "Plays a sound - use Sound and AudioDecoder SPI"() {
		given:
			var oggStream = getResourceAsStream('nz/net/ultraq/redhorizon/audio/AudioCheck_Sound_bong_001.ogg')
			var sound = new Sound('AudioCheck_Sound_bong_001.ogg', oggStream)
		when:
			sound.play()
			while (!sound.stopped) {
				sound.update()
				Thread.sleep(500)
			}
		then:
			notThrown(Exception)
		cleanup:
			sound?.close()
			oggStream?.close()
	}
}
