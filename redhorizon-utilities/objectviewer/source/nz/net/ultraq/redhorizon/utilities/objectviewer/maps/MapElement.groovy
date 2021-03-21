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

package nz.net.ultraq.redhorizon.utilities.objectviewer.maps

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.ShaderType
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import org.joml.Rectanglef
import org.joml.Vector2f

import java.nio.ByteBuffer

/**
 * The graphical form of a map tile.
 * 
 * @author Emanuel Rabina
 */
class MapElement implements GraphicsElement {

	final int width
	final int height
	final ColourFormat format
	private ByteBuffer imageData
	private final Vector2f position

	private Material material

	/**
	 * Constructor, create a  map element from a map sprite, set to the given
	 * position.
	 * 
	 * @param imagesFile
	 * @param frame
	 * @param position
	 */
	MapElement(ImagesFile imagesFile, int frame, Vector2f position) {

		this.width     = imagesFile.width
		this.height    = imagesFile.height
		this.format    = imagesFile.format
		this.imageData = imagesFile.imagesData[frame].flipVertical(width, height, format)
		this.position  = position
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		material = renderer.createMaterial(
			renderer.createSpriteMesh(new Rectanglef(position.x, position.y, position.x + width as float, position.y + height as float)),
			renderer.createTexture(imageData, format.value, width, height),
			ShaderType.TEXTURE_PALETTE
		)
		imageData = null
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(material)
	}
}
