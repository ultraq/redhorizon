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

package nz.net.ultraq.redhorizon.engine.media

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.opengl.SpriteShader
import nz.net.ultraq.redhorizon.engine.resources.Resource
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.primitives.Rectanglef

import java.nio.ByteBuffer

/**
 * A basic image / texture / 2D sprite.
 *
 * @author Emanuel Rabina
 */
class Image extends Node<Image> implements Resource, GraphicsElement {

	final int width
	final int height
	final ColourFormat format
	private ByteBuffer imageData

	private Mesh mesh
	private Shader shader
	private Material material

	/**
	 * Constructor, creates an image out of the given image file data.
	 *
	 * @param imageFile
	 *   Image source.
	 * @param palette
	 *   If the image data requires a palette, then this is used to complete it.
	 */
	Image(ImageFile imageFile, Palette palette = null) {

		this(imageFile.width, imageFile.height, imageFile.format, imageFile.imageData, palette)
	}

	/**
	 * Constructor, creates an image out of a specific frame in a multi-image
	 * file.
	 *
	 * @param imagesFile
	 *   Image source.
	 * @param frame
	 *   The specific frame in the source to use
	 * @param palette
	 *   If the image data requires a palette, then this is used to complete it.
	 */
	Image(ImagesFile imagesFile, int frame, Palette palette = null) {

		this(imagesFile.width, imagesFile.height, imagesFile.format, imagesFile.imagesData[frame], palette)
	}

	/**
	 * Constructor, creates an image from the given data.
	 *
	 * @param width
	 * @param height
	 * @param format
	 * @param imageData
	 * @param palette
	 */
	Image(int width, int height, ColourFormat format, ByteBuffer imageData, Palette palette = null) {

		this.width = width
		this.height = height
		this.format = palette?.format ?: format
		this.imageData = (palette ? imageData.applyPalette(palette) : imageData).flipVertical(width, height, this.format)

		this.bounds.set(0, 0, width, height)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		mesh = renderer.createSpriteMesh(
			surface: new Rectanglef(0, 0, width, height)
		)
		shader = renderer.getShader(SpriteShader.NAME)
		material = renderer.createMaterial(
			texture: renderer.createTexture(width, height, format, imageData),
			transform: transform
		)
		imageData = null
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.draw(mesh, shader, material)
	}
}
