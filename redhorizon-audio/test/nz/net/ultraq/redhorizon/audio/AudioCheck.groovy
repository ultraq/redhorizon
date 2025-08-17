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

import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat.Encoding
import javax.sound.sampled.AudioSystem

/**
 * A simple class to play back a sound using the audio module.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class AudioCheck extends Specification {

	static {
		System.setProperty('org.lwjgl.system.stackSize', '10240')
	}

	AudioDevice device

	def setup() {
		device = new OpenALAudioDevice()
	}

	def cleanup() {
		device.close()
	}

	def "Plays a sound - pre-decoded data"() {
		when:
			AudioSystem.getAudioInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/audio/AudioCheck.ogg')).withCloseable { oggStream ->
				AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, oggStream).withCloseable { pcmStream ->
					var format = pcmStream.format
					new Sound(format.sampleSizeInBits, format.channels, (int)format.sampleRate, ByteBuffer.wrapNative(pcmStream.readAllBytes())).withCloseable { sound ->
						sound.play()
						while (sound.playing) {
							Thread.sleep(100)
						}
					}
				}
			}
		then:
			notThrown(Exception)
	}

	def "Plays a sound - use SPI to decode data"() {
		when:
			getResourceAsStream('nz/net/ultraq/redhorizon/audio/AudioCheck.ogg').withCloseable { inputStream ->
				new Sound('AudioCheck.ogg', inputStream).withCloseable { sound ->
					sound.play()
					while (sound.playing) {
						Thread.sleep(100)
					}
				}
			}
		then:
			notThrown(Exception)
	}
}
