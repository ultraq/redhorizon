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
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Rectanglef

import java.nio.ByteBuffer

/**
 * A basic image / texture / 2D sprite.
 * 
 * @author Emanuel Rabina
 */
class Image implements GraphicsElement, SelfVisitable {

	// Image attributes
	final int width
	final int height
	final int format
	final ByteBuffer imageData
	final Rectanglef dimensions

	// Rendering information
	private int textureId

	/**
	 * Constructor, creates an image out of the given image file data.
	 * 
	 * @param imageFile  Image source.
	 * @param dimensions Dimensions over which to display the image over.
	 */
	Image(ImageFile imageFile, Rectanglef dimensions) {

		width     = imageFile.width
		height    = imageFile.height
		format    = imageFile.format.value
		imageData = ByteBuffer.fromBuffersDirect(imageFile.imageData)
		this.dimensions = dimensions
	}

//	/**
//	 * Constructor, creates an image from a file containing several images.
//	 * 
//	 * @param imagesfile The file containing several images, including the one
//	 * 					 to make.
//	 * @param imagenum	 The 0-index image number to use for this object.
//	 */
//	Image(ImagesFile imagesfile, int imagenum) {
//
//		this(imagesfile.filename() + "_" + imagenum, imagesfile.format(), imagesfile.width(), imagesfile.height(),
//			(imagesfile instanceof Paletted) ? ((Paletted)imagesfile).applyPalette(ResourceManager.getPalette())[imagenum]:
//			imagesfile.getImages()[imagenum], (imagesfile instanceof Paletted))
//	}

//	/**
//	 * Constructor, creates an image using the given image parts.
//	 * 
//	 * @param name	   The name of the image.
//	 * @param format   RGB/A format of the image.
//	 * @param width	   The width of the image.
//	 * @param height   The height of the image.
//	 * @param image	   The bytes consisting of the image.
//	 * @param paletted Whether the file is paletted or not.
//	 */
//	Image(String name, int format, int width, int height, ByteBuffer image, boolean paletted) {
//
//		super(name + (paletted ? "_" + ResourceManager.getPalette() : ""))
//		this.format = format
//		this.coords = new Rectangle2D(-width >> 1, -height >> 1, width >> 1, height >> 1)
//		this.image  = image
//
//		texturewidth  = width
//		textureheight = height
//	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteTextures(textureId)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		textureId = renderer.createTexture(imageData, format, width, height)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawTexture(textureId, dimensions)
	}
}
