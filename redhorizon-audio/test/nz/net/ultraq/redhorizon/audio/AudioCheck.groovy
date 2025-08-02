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
import nz.net.ultraq.redhorizon.audio.openal.OpenALBuffer
import nz.net.ultraq.redhorizon.audio.openal.OpenALSource

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

	def "Plays a sound"() {
		when:
			new OpenALAudioDevice().withCloseable { device ->
				AudioSystem.getAudioInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/audio/AudioCheck.ogg')).withCloseable { oggStream ->
					AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, oggStream).withCloseable { pcmStream ->
						new OpenALBuffer(16, 2, 44100, ByteBuffer.wrapNative(pcmStream.readAllBytes())).withCloseable { buffer ->
							new OpenALSource().withCloseable { source ->
								source
									.attachBuffer(buffer)
									.play()
								while (source.playing) {
									Thread.sleep(100)
								}
							}
						}
					}
				}
			}
		then:
			notThrown(Exception)
	}
}
