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
import nz.net.ultraq.redhorizon.engine.graphics.ShaderType
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.scenegraph.SceneElement

import org.joml.Rectanglef

import java.nio.ByteBuffer

/**
 * The graphical form of a map tile.
 * 
 * @author Emanuel Rabina
 */
class MapElement implements GraphicsElement, SceneElement<MapElement> {

	final int width
	final int height
	final ColourFormat format
	private ByteBuffer imageData

	private Material material

	/**
	 * Constructor, create a  map element from a map sprite, set to the given
	 * position.
	 * 
	 * @param imagesFile
	 * @param frame
	 */
	MapElement(ImagesFile imagesFile, int frame) {

		this.width     = imagesFile.width
		this.height    = imagesFile.height
		this.format    = imagesFile.format
		this.imageData = imagesFile.imagesData[frame].flipVertical(width, height, format)

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
			ShaderType.STANDARD_PALETTE
		)
		imageData = null
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(material, transform)
	}
}
