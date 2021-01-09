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
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import org.joml.Rectanglef

import java.nio.ByteBuffer

/**
 * A basic image / texture / 2D sprite.
 * 
 * @author Emanuel Rabina
 */
class Image implements GraphicsElement, SelfVisitable {

	@Delegate
	final Texture texture
	final Rectanglef dimensions

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
			imageFile.format !== FORMAT_INDEXED ? imageFile.format.value : palette.format.value,
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
			imagesFile.format !== FORMAT_INDEXED ? imagesFile.format.value : palette.format.value,
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
	Image(int width, int height, int format, ByteBuffer imageData, Rectanglef dimensions, float repeatX = 1, float repeatY = 1) {

		this(new Texture(width, height, format, imageData, null, repeatX, repeatY), dimensions)
	}

	/**
	 * Constructor, creates an image from new or existing texture data.
	 * 
	 * @param texture    A texture built from new data, or an existing texture so
	 *                   that image data can be reused.
	 * @param dimensions Dimensions over which to display the image over.
	 */
	Image(Texture texture, Rectanglef dimensions) {

		this.texture    = texture
		this.dimensions = dimensions
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawTexture(textureId, dimensions, repeatX, repeatY, true)
	}
}
