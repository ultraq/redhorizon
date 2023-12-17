/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli.mediaplayer

import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.engine.ApplicationStartedEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.PcxFile

import spock.lang.Specification

/**
 * Tests to ensure the media player can still run.
 *
 * @author Emanuel Rabina
 */
class MediaPlayerTests extends Specification {

	private static Palette palette

	def setupSpec() {

		System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
		System.setProperty('org.lwjgl.system.stackSize', '10240')

		palette = getResourceAsStream('ra-temperate.pal').withBufferedStream { inputStream ->
			return new PalFile(inputStream)
		}
	}

	def "Opens an image file"() {
		when:
			var mediaPlayer = new MediaPlayer(
				new PcxFile(getResourceAsStream('nz/net/ultraq/redhorizon/cli/mediaplayer/alipaper.pcx')),
				new AudioConfiguration(),
				new GraphicsConfiguration(),
				palette
			)
			mediaPlayer.on(ApplicationStartedEvent) { event ->
				Thread.sleep(3000)
				mediaPlayer.stop()
			}
			mediaPlayer.start()
		then:
			notThrown(Exception)
	}
}
