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

package nz.net.ultraq.redhorizon.utilities.mediaplayer

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import nz.net.ultraq.redhorizon.scenegraph.SceneVisitor

import org.joml.Vector3f

/**
 * A series of images laid out in a long horizontal strip.
 * 
 * @author Emanuel Rabina
 */
class ImageStrip implements GraphicsElement, SceneElement {

	final List<Image> images = []
	final Palette palette
	private Texture paletteTexture

	/**
	 * Constructor, build a strip of images from a file containing multiple
	 * images.
	 *
	 * @param imagesFile
	 * @param palette
	 */
	ImageStrip(ImagesFile imagesFile, Palette palette) {

		this.palette = palette
		this.bounds.set(0, 0, imagesFile.width * imagesFile.numImages, imagesFile.height)

		imagesFile.numImages.times { i ->
			def image = new Image(imagesFile, i)
			image.translate(new Vector3f(imagesFile.width * i, -imagesFile.height / 2, 0))
			images << image
		}
	}

	@Override
	void accept(SceneVisitor visitor) {

		visitor.visit(this)
		images.each { image ->
			visitor.visit(image)
		}
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteTexture(paletteTexture)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		paletteTexture = renderer.createTexturePalette(palette)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.setPalette(paletteTexture)
	}
}
