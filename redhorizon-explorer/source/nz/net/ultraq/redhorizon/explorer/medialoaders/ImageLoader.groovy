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

package nz.net.ultraq.redhorizon.explorer.medialoaders

import nz.net.ultraq.redhorizon.engine.media.Image
import nz.net.ultraq.redhorizon.filetypes.ImageFile

import groovy.transform.InheritConstructors

/**
 * Load a single image into existing engines.
 *
 * @author Emanuel Rabina
 */
@InheritConstructors
class ImageLoader extends MediaLoader<ImageFile, Image> {

	@Override
	Image load() {

		def width = file.width
		def height = file.height
		media = new Image(file)
			.scaleXY(scene.window.renderResolution.calculateScaleToFit(width, height))
			.translate(-width / 2, -height / 2)
		scene << media

		return media
	}

	@Override
	void unload() {

		scene.removeNode(media)
	}
}
