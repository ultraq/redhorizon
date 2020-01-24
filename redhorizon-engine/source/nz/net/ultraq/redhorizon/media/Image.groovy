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
	final boolean filter

	// Rendering information
	private int textureId

	/**
	 * Constructor, creates an image out of the given image file data.
	 * 
	 * @param imageFile  Image source.
	 * @param dimensions Dimensions over which to display the image over.
	 * @param filter     Filter the image.
	 */
	Image(ImageFile imageFile, Rectanglef dimensions, boolean filter) {

		this(imageFile.width, imageFile.height, imageFile.format.value, imageFile.imageData, dimensions, filter)
	}

	/**
	 * Constructor, creates an image from the given data.
	 * 
	 * @param width
	 * @param height
	 * @param format
	 * @param imageData
	 * @param dimensions
	 * @param filter
	 */
	Image(int width, int height, int format, ByteBuffer imageData, Rectanglef dimensions, boolean filter) {

		this.width      = width
		this.height     = height
		this.format     = format
		this.imageData  = ByteBuffer.fromBuffersDirect(imageData)
		this.dimensions = dimensions
		this.filter     = filter
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteTextures(textureId)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		textureId = renderer.createTexture(imageData, format, width, height, filter)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawTexture(textureId, dimensions)
	}
}
