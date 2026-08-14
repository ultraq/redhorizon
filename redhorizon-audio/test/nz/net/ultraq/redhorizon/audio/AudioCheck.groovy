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

import nz.net.ultraq.redhorizon.audio.openal.OpenALDevice

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

	Device device

	def setup() {
		device = new OpenALDevice()
	}

	def cleanup() {
		device.close()
	}

	def "Plays a sound - use Sound and AudioDecoder SPI"() {
		given:
			var listener = new AudioListener()
				.withGain(0.5f)
			var data = getResourceAsStream('nz/net/ultraq/redhorizon/audio/AudioCheck_Sound_bong_001.ogg').withBufferedStream { stream ->
				return new AudioData('AudioCheck_Sound_bong_001.ogg', stream)
			}
			var source = new AudioSource(data)
		when:
			source.play()
			while (!source.stopped) {
				listener.render()
				source.render()
				Thread.sleep(500)
			}
		then:
			noExceptionThrown()
		cleanup:
			source?.close()
			data?.close()
	}
}
