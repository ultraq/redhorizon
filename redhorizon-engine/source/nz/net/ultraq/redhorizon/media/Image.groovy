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
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import org.joml.Rectanglef

import groovy.transform.MapConstructor
import java.nio.ByteBuffer

/**
 * A basic image / texture / 2D sprite.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor
class Image implements GraphicsElement, SelfVisitable {

	final int width
	final int height
	final ColourFormat format
	final Rectanglef dimensions
	private ByteBuffer imageData
	final float repeatX
	final float repeatY

	private Material material

	/**
	 * Constructor, creates an image out of the given image file data.
	 * 
	 * @param imageFile  Image source.
	 * @param dimensions Dimensions over which to display the image over.
	 * @param palette    If the image data requires a palette, then this is used
	 *                   to complete it.
	 */
	Image(ImageFile imageFile, Rectanglef dimensions, Palette palette = null) {

		this(imageFile.width, imageFile.height,
			imageFile.format !== FORMAT_INDEXED ? imageFile.format : palette.format,
			imageFile.format !== FORMAT_INDEXED ? imageFile.imageData : imageFile.imageData.applyPalette(palette),
			dimensions)
	}

	/**
	 * Constructor, creates an image out of a specific frame in a multi-image
	 * file.
	 * 
	 * @param imagesFile Image source.
	 * @param frame      The specific frame in the source to use
	 * @param dimentions Dimensions over which to display the image over.
	 * @param palette    If the image data requires a palette, then this is used
	 *                   to complete it.
	 */
	Image(ImagesFile imagesFile, int frame, Rectanglef dimensions, Palette palette = null) {

		this(imagesFile.width, imagesFile.height,
			imagesFile.format !== FORMAT_INDEXED ? imagesFile.format : palette.format,
			imagesFile.format !== FORMAT_INDEXED ? imagesFile.imagesData[frame] : imagesFile.imagesData[frame].applyPalette(palette),
			dimensions)
	}

	/**
	 * Constructor, creates an image from the given data.
	 * 
	 * @param width
	 * @param height
	 * @param format
	 * @param imageData
	 * @param dimensions Dimensions over which to display the image over.
	 * @param repeatX
	 * @param repeatY
	 */
	Image(int width, int height, ColourFormat format, ByteBuffer imageData, Rectanglef dimensions, float repeatX = 1, float repeatY = 1) {

		this.width      = width
		this.height     = height
		this.format     = format
		this.imageData  = imageData.flipVertical(width, height, format)
		this.dimensions = dimensions
		this.repeatX    = repeatX
		this.repeatY    = repeatY
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		material = renderer.createMaterial(
			renderer.createSpriteMesh(dimensions, repeatX, repeatY),
			renderer.createTexture(imageData, format.value, width, height)
		)
		imageData = null
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(material)
	}
}
