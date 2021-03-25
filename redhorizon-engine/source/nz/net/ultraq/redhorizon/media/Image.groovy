/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.ShaderType
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.scenegraph.SceneElement

import org.joml.Rectanglef

import java.nio.ByteBuffer

/**
 * A basic image / texture / 2D sprite.
 * 
 * @author Emanuel Rabina
 */
class Image implements GraphicsElement, SceneElement {

	final int width
	final int height
	final ColourFormat format
	private ByteBuffer imageData

	private Material material

	/**
	 * Constructor, creates an image out of the given image file data.
	 * 
	 * @param imageFile Image source.
	 */
	Image(ImageFile imageFile) {

		this(imageFile.width, imageFile.height, imageFile.format, imageFile.imageData)
	}

	/**
	 * Constructor, creates an image out of a specific frame in a multi-image
	 * file.
	 * 
	 * @param imagesFile Image source.
	 * @param frame      The specific frame in the source to use
	 */
	Image(ImagesFile imagesFile, int frame) {

		this(imagesFile.width, imagesFile.height, imagesFile.format, imagesFile.imagesData[frame])
	}

	/**
	 * Constructor, creates an image from the given data.
	 * 
	 * @param width
	 * @param height
	 * @param format
	 * @param imageData
	 */
	Image(int width, int height, ColourFormat format, ByteBuffer imageData) {

		this.width     = width
		this.height    = height
		this.format    = format
		this.imageData = imageData.flipVertical(width, height, format)

		this.bounds.set(0, 0, width, height)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		material = renderer.createMaterial(
			renderer.createSpriteMesh(new Rectanglef(0, 0, width, height)),
			renderer.createTexture(imageData, format.value, width, height),
			format === ColourFormat.FORMAT_INDEXED ? ShaderType.TEXTURE_PALETTE : ShaderType.TEXTURE
		)
		imageData = null
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(material, transform)
	}
}
