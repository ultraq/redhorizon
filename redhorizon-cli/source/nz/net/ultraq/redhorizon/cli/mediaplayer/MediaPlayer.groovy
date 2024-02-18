/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Animation
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.FullScreenContainer
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sound
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Video
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ResourceFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Media player application for watching media files.
 *
 * @author Emanuel Rabina
 */
class MediaPlayer {

	// List of hi-res PCX images used: aftr_hi, alipaper, aly1, apc_hi, aphi0049,
	// bnhi0020, dchi0040, frhi0166, lab, landsbrg, mahi0107, mig_hi, mtfacthi,
	// needle, sov2, spy, stalin, tent.

	// List of soundtrack files available: intro, map, await, bigf226m, crus226m,
	// dense_r, fac1226m, fac2226m, fogger1a, hell226m, mud1a, radio2, rollout,
	// run1226m, smsh226m, snake, tren226m, terminat, twin, vector1a, work226m.

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	/**
	 * Constructor, create a new application around the given media file.
	 */
	MediaPlayer(ResourceFile resourceFile, AudioConfiguration audioConfig, GraphicsConfiguration graphicsConfig) {

		var scene = new Scene()
		Node mediaNode = null

		new Application('Media Player')
			.addAudioSystem(audioConfig)
			.addGraphicsSystem(graphicsConfig)
			.addTimeSystem()
			.useScene(scene)
			.onApplicationStart(application -> {
				logger.info('File details: {}', resourceFile)

				mediaNode = switch (resourceFile) {
					case ImageFile ->
						new FullScreenContainer().addChild(new Sprite(resourceFile))
					case VideoFile ->
						new FullScreenContainer().addChild(new Video(resourceFile).attachScript(new PlaybackScript(application, true)))
					case AnimationFile ->
						new FullScreenContainer().addChild(new Animation(resourceFile).attachScript(new PlaybackScript(application, true)))
					case SoundFile ->
						new Sound(resourceFile).attachScript(new PlaybackScript(application, resourceFile.forStreaming))
					default ->
						throw new UnsupportedOperationException("No media script for the associated file class of ${resourceFile}")
				}

				scene << mediaNode
			})
			.onApplicationStop(application -> {
				scene.removeNode(mediaNode)
			})
			.start()
	}
}
