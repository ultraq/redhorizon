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

package nz.net.ultraq.redhorizon.classic.maps

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.opengl.SpriteShader
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.primitives.Rectanglef

import java.nio.ByteBuffer

/**
 * A repeated texture stretched over the entirety of the possible map area.
 *
 * @author Emanuel Rabina
 */
class MapBackground implements GraphicsElement, Node {

	final int width
	final int height
	final ColourFormat format
	private ByteBuffer imageData
	final float repeatX
	final float repeatY

	private Mesh mesh
	private Shader shader
	private Material material

	/**
	 * Constructor, set the image and area the background will be stretched over.
	 *
	 * @param imageWidth
	 * @param imageHeight
	 * @param imageData
	 * @param repeatX
	 * @param repeatY
	 * @param palette
	 */
	MapBackground(int imageWidth, int imageHeight, ByteBuffer imageData, float repeatX, float repeatY, Palette palette) {

		this.width = imageWidth
		this.height = imageHeight
		this.format = palette.format
		this.imageData = imageData.applyPalette(palette).flipVertical(width, height, format)
		this.repeatX = repeatX
		this.repeatY = repeatY

		this.bounds.set(0, 0, width * repeatX as float, height * repeatY as float)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
		renderer.deleteMesh(mesh)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		mesh = renderer.createSpriteMesh(
			surface: new Rectanglef(0, 0, width * repeatX as float, height * repeatY as float),
			textureUVs: new Rectanglef(0, 0, repeatX, repeatY)
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
