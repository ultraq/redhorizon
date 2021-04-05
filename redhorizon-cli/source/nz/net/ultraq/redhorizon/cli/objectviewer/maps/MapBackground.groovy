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

package nz.net.ultraq.redhorizon.cli.objectviewer.maps

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.scenegraph.SceneElement

import org.joml.Rectanglef

import java.nio.ByteBuffer

/**
 * A repeated texture stretched over the entirety of the possible map area.
 * 
 * @author Emanuel Rabina
 */
class MapBackground implements GraphicsElement, SceneElement<MapBackground> {

	final int width
	final int height
	final ColourFormat format
	private ByteBuffer imageData
	final float repeatX
	final float repeatY
	private Palette palette

	private Material material

	/**
	 * Constructor, set the image and area the background will be stretched over.
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @param imageFormat
	 * @param imageData
	 * @param repeatX
	 * @param repeatY
	 * @param palette
	 */
	MapBackground(int imageWidth, int imageHeight, ColourFormat imageFormat, ByteBuffer imageData, float repeatX, float repeatY,
		Palette palette) {

		this.width     = imageWidth
		this.height    = imageHeight
		this.format    = imageFormat
		this.imageData = imageData.flipVertical(width, height, format)
		this.repeatX   = repeatX
		this.repeatY   = repeatY
		this.palette   = palette

		this.bounds.set(0, 0, width * repeatX as float, height * repeatY as float)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		material = new Material(
			mesh: renderer.createSpriteMesh(new Rectanglef(0, 0, width * repeatX as float, height * repeatY as float), repeatX, repeatY),
			texture: renderer.createTexture(imageData, format.value, width, height),
			palette: renderer.createTexturePalette(palette),
			shader: renderer.standardPaletteShader
		)
		imageData = null
		palette = null
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(material, transform)
	}
}
