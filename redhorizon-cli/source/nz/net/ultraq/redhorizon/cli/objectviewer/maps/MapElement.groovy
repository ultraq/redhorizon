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
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.scenegraph.SceneElement

import org.joml.Rectanglef

/**
 * The graphical form of a map tile.
 * 
 * @author Emanuel Rabina
 */
class MapElement implements GraphicsElement, SceneElement<MapElement> {

	final TileSet tileSet
	final ImagesFile tileFile
	final int frame

	private Material material

	/**
	 * Constructor, create a  map element from a map sprite, set to the given
	 * position.
	 * 
	 * @param tileSet
	 * @param tileFile
	 * @param frame
	 */
	MapElement(TileSet tileSet, ImagesFile tileFile, int frame) {

		this.tileSet  = tileSet
		this.tileFile = tileFile
		this.frame    = frame

		this.bounds.set(0, 0, tileFile.width, tileFile.height)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		material = renderer.createMaterial(
			renderer.createSpriteMesh(
				new Rectanglef(0, 0, tileFile.width, tileFile.height),
				tileSet.getCoordinates(tileFile, frame)),
			tileSet.texture,
			transform
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.drawMaterial(material)
	}
}
