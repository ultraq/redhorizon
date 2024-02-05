/*
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneVisitor
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

/**
 * A series of images laid out in a long horizontal strip.
 *
 * @author Emanuel Rabina
 */
class ImageStrip extends Node<ImageStrip> {

	final List<Image> images = []

	/**
	 * Constructor, build a strip of images from a file containing multiple
	 * images.
	 *
	 * @param imagesFile
	 * @param palette
	 */
	ImageStrip(ImagesFile imagesFile, Palette palette) {

		this.bounds.set(0, 0, imagesFile.width * imagesFile.numImages, imagesFile.height)
		translate(0, -imagesFile.height / 2 as float)

		imagesFile.numImages.times { i ->
			images << new Image(imagesFile, i, palette)
				.translate(imagesFile.width * i, -imagesFile.height / 2)
		}
	}

	@Override
	void accept(SceneVisitor visitor) {

		visitor.visit(this)
		images*.accept(visitor)
	}
}
