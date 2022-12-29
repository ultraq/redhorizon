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
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import org.joml.primitives.Rectanglef

import java.nio.ByteBuffer

/**
 * A texture made to contain all of the textures that make up the current map so
 * that the entire tileset can be bound to a single texture slot.  Individual
 * textures are then picked out by their coordinates within the overall tileset.
 * 
 * @author Emanuel Rabina
 */
class TileSet implements GraphicsElement {

	private final int tilesetWidth = 1536
	private final int tilesetHeight = 4800
	private final ByteBuffer tilesetData

	final Palette palette

	private final List<ImagesFile> tileFileList = []
	private final Map<ImagesFile,Integer> tileFileMap = [:]
	private Texture texture

	/**
	 * Constructor, build a tileset to fit data that uses the given palette.
	 * 
	 * @param palette
	 */
	TileSet(Palette palette) {

		this.palette = palette
		tilesetData = ByteBuffer.allocateNative(tilesetWidth * tilesetHeight * palette.format.value)
	}

	/**
	 * Add more tiles to the tileset.
	 * 
	 * @param tilesFile
	 */
	void addTiles(ImagesFile tilesFile) {

		if (tilesFile.format != FORMAT_INDEXED) {
			throw new IllegalArgumentException('Texture atlas currently only supports indexed image data')
		}

		// File already added
		if (tileFileList.contains(tilesFile)) {
			return
		}

		// Find the last available Y axis to insert this image data into
		def yStart = tileFileList.inject(0) { acc, images ->
			return acc + images.height
		}

		// Insert image data into the next rows here
		def tileWidth = tilesFile.width * palette.format.value
		tilesFile.imagesData.eachWithIndex { imageData, i ->
			def colouredImageData = imageData.applyPalette(palette)
			tilesFile.height.times { y ->
				tilesetData
					.position(((yStart + y) * tilesetWidth * palette.format.value) + (i * tileWidth))
					.put(colouredImageData.array(), colouredImageData.position() + (y * tileWidth), tileWidth)
			}
		}
		tilesetData.rewind()

		tileFileList << tilesFile
		tileFileMap << [(tilesFile): yStart]
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteTexture(texture)
	}

	/**
	 * Return the rectangle representing the texture coordinates in the tileset
	 * for the given tile file and frame.
	 * 
	 * @param tileFile
	 * @param frame
	 * @return
	 */
	Rectanglef getCoordinates(ImagesFile tileFile, int frame) {

		def xStart = tileFile.width * frame
		def yStart = tileFileMap[tileFile]
		return new Rectanglef(
			xStart / tilesetWidth,
			1 - ((yStart + tileFile.height) / tilesetHeight),
			(xStart + tileFile.width) / tilesetWidth,
			1 - (yStart / tilesetHeight)
		)
	}

	/**
	 * Return the texture underlying this tileset, if available.
	 * 
	 * @return
	 */
	Texture getTexture() {

		return texture
	}

	@Override
	void init(GraphicsRenderer renderer) {

		texture = renderer.createTexture(tilesetWidth, tilesetHeight, palette.format.value,
			tilesetData.flipVertical(tilesetWidth, tilesetHeight, palette.format)
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {
	}
}
