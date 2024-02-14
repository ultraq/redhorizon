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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.opengl.SpriteShader
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import org.joml.primitives.Rectanglef

/**
 * The graphical form of a map tile.
 *
 * @author Emanuel Rabina
 */
class MapElement extends Node<MapElement> implements GraphicsElement {

	final TileSet tileSet
	final ImagesFile tileFile
	final int frame

	private Mesh mesh
	private Shader shader
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

		this.tileSet = tileSet
		this.tileFile = tileFile
		this.frame = frame

		this.bounds.set(0, 0, tileFile.width, tileFile.height)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMaterial(material)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		mesh = renderer.createSpriteMesh(
			surface: new Rectanglef(0, 0, tileFile.width, tileFile.height),
			textureUVs: tileSet.getCoordinates(tileFile, frame)
		)
		shader = renderer.getShader(SpriteShader.NAME)
		material = renderer.createMaterial(
			texture: tileSet.texture,
			transform: transform
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.draw(mesh, shader, material)
	}
}
