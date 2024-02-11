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
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Animation
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.FullScreenContainer
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sound
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.ResourceFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

/**
 * Media player application for watching media files.
 *
 * @author Emanuel Rabina
 */
class MediaPlayer extends Application {

	// List of hi-res PCX images used: aftr_hi, alipaper, aly1, apc_hi, aphi0049,
	// bnhi0020, dchi0040, frhi0166, lab, landsbrg, mahi0107, mig_hi, mtfacthi,
	// needle, sov2, spy, stalin, tent.

	// List of soundtrack files available: intro, map, await, bigf226m, crus226m,
	// dense_r, fac1226m, fac2226m, fogger1a, hell226m, mud1a, radio2, rollout,
	// run1226m, smsh226m, snake, tren226m, terminat, twin, vector1a, work226m.

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	private final ResourceFile mediaFile
	private final Palette palette
	private Node media

	/**
	 * Constructor, create a new application around the given media file.
	 */
	MediaPlayer(ResourceFile resourceFile, AudioConfiguration audioConfig, GraphicsConfiguration graphicsConfig, Palette palette) {

		super('Media Player', audioConfig, graphicsConfig)
		this.mediaFile = resourceFile
		this.palette = palette
	}

	@Override
	protected void applicationStart() {

		logger.info('File details: {}', mediaFile)

		var mediaNode = switch (mediaFile) {
			case ImageFile ->
				new FullScreenContainer().addChild(new Sprite(mediaFile))
			case AnimationFile ->
				new FullScreenContainer().addChild(new Animation(mediaFile).attachScript(new PlaybackScript(this, true)))
			case SoundFile ->
				new Sound(mediaFile).attachScript(new PlaybackScript(this, mediaFile.forStreaming))
			default ->
				throw new UnsupportedOperationException("No media script for the associated file class of ${mediaFile}")
		}

		scene << mediaNode

//		mediaLoader = switch (mediaFile) {
//			case VideoFile -> new VideoLoader(mediaFile, scene, graphicsSystem, gameClock, inputEventStream)
//			case AnimationFile -> new AnimationLoader(mediaFile, scene, graphicsSystem, gameClock, inputEventStream)
//			case SoundFile -> new SoundLoader(mediaFile, scene, gameClock, inputEventStream)
//			case ImageFile -> new ImageLoader(mediaFile, scene)
//			case ImagesFile -> new ImagesLoader(mediaFile, palette, scene, graphicsSystem, inputEventStream)
//			default -> throw new UnsupportedOperationException("No media player for the associated file class of ${mediaFile}")
//		}
//
//		var media = mediaLoader.load()
//		if (media instanceof Playable) {
//			media.on(StopEvent) { event ->
//				stop()
//			}
//			media.play()
//		}

		// Universal quit on exit
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS && event.key == GLFW_KEY_ESCAPE) {
				stop()
			}
		}
	}

	@Override
	protected void applicationStop() {

		scene.removeNode(media)
	}
}
