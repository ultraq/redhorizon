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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.engine.GameClock
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.scenegraph.Scene

import groovy.transform.TupleConstructor

/**
 * Load a video file into existing engines.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class VideoLoader implements MediaLoader<VideoFile, Video> {

	final Scene scene
	final GraphicsEngine graphicsEngine
	final GameClock gameClock

	@Override
	Video load(VideoFile videoFile) {

		def width = videoFile.width
		def height = videoFile.height
		def scaleY = videoFile.forVgaMonitors ? 1.2f : 1f
		def scale = graphicsEngine.graphicsContext.renderResolution.calculateScaleToFit(width, height * scaleY as int)

		def video = new Video(videoFile, gameClock)
			.scale(scale, scale * scaleY as float, 1)
			.translate(-width / 2, -height / 2)
		scene << video
		return video
	}
}
