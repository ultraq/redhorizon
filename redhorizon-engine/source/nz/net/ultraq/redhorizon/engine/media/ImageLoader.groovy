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

package nz.net.ultraq.redhorizon.engine.media

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.ImageFile

/**
 * Load a single image into existing engines.
 *
 * @author Emanuel Rabina
 */
class ImageLoader extends MediaLoader<ImageFile, Image> {

	private final GraphicsEngine graphicsEngine

	/**
	 * Constructor, create a loader for an image file.
	 *
	 * @param imageFile
	 * @param scene
	 * @param graphicsEngine
	 */
	ImageLoader(ImageFile imageFile, Scene scene, GraphicsEngine graphicsEngine) {

		super(imageFile, scene)
		this.graphicsEngine = graphicsEngine
	}

	@Override
	Image load() {

		def width = file.width
		def height = file.height
		media = new Image(file)
			.scaleXY(graphicsEngine.window.renderResolution.calculateScaleToFit(width, height))
			.translate(-width / 2, -height / 2)
		scene << media

		return media
	}

	@Override
	void unload() {

		scene.removeSceneElement(media)
	}
}
