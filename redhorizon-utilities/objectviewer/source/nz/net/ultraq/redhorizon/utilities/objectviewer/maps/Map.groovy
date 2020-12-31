/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.classic.codecs.PackData
import nz.net.ultraq.redhorizon.classic.filetypes.ini.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.pal.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.tmp.TmpFileRA
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Rectanglef
import org.joml.Rectanglei
import org.joml.Vector2f

import groovy.transform.Memoized
import java.nio.ByteBuffer

/**
 * A map on which a mission or a skirmish can take place.
 * 
 * @author Emanuel Rabina
 */
class Map implements GraphicsElement, SelfVisitable {

	private static final int TILE_WIDTH = 24
	private static final int TILE_HEIGHT = 24
	private static final int TILES_HORIZONTAL = 128
	private static final int TILES_VERTICAL = 128

	final String name
	final Theaters theater
	final Rectanglei boundary
	final Vector2f initialPosition
	private Image background
	private final List<Image> tiles = []

	/**
	 * Construtor, build a map from the given map file.
	 * 
	 * @param mapFile
	 */
	Map(IniFile mapFile) {

		name = mapFile['Basic']['Name']

		def mapSection = mapFile['Map']
		def theaterString = mapSection['Theater']
		theater = Theaters.find { theater ->
			return theater.label.equalsIgnoreCase(theaterString)
		}
		def palette = getResourceAsBufferedStream("ra-${theater.label}.pal").withStream { inputStream ->
			return new PalFile(inputStream).withAlphaMask()
		}
		def mapX = mapSection['X'] as int
		def mapY = mapSection['Y'] as int
		boundary = new Rectanglei(
			mapX,
			mapY,
			mapX + (mapSection['Width'] as int),
			mapY + (mapSection['Height'] as int)
		)

		def waypoints = mapFile['Waypoints']
		def waypoint98 = waypoints['98'] as int
		initialPosition = cellNumberAsWorldCoords(waypoint98)

		// TODO: Figure out some way to share knowledge of the path to mix files
		//       containing the necessary files
		new MixFile(new File("mix/red-alert/MapTiles_${theater.label}.mix")).withCloseable { tilesetMixFile ->
			def clearTileName = TilesRA.DEFAULT.name + theater.ext
			def backgroundTileFile = tilesetMixFile.getEntryData(tilesetMixFile.getEntry(clearTileName)).withStream { inputStream ->
				return new TmpFileRA(inputStream)
			}

			// Use the background tile to create a 5x4 repeating image
			def combinedBackgroundData = backgroundTileFile.imagesData
				.combineImages(backgroundTileFile.width, backgroundTileFile.height, 5)
				.applyPalette(palette)
			def combinedWidth = backgroundTileFile.width * 5
			def combinedHeight = backgroundTileFile.height * 4
			background = new Image(combinedWidth, combinedHeight, palette.format.value, combinedBackgroundData,
				new Rectanglef(0, 0, TILES_HORIZONTAL * TILE_WIDTH, TILES_VERTICAL * TILE_HEIGHT),
				TILES_HORIZONTAL * TILE_WIDTH / combinedWidth, TILES_VERTICAL * TILE_HEIGHT / combinedHeight
			)

			// Decode the Mappack section to get all the tiles needed to build the map
			def mapPack = mapFile['MapPack']
			def mapPackBytes = mapDataToBytes(mapPack)
			(boundary.minY..<boundary.maxY).each { y ->
				(boundary.minX..<boundary.maxX).each { x ->

					// Get the byte representing the tile
					def tilePos = cellCoordsAsNumber(x, y)
					def tileVal = mapPackBytes.getShort(2 * tilePos)
					def tilePic = mapPackBytes.get(TILES_HORIZONTAL * TILES_VERTICAL * 2 + tilePos)

					// Retrieve the appropriate tile, skip empty tiles
					if (tileVal != 0xff && tileVal != -1) {
						def tile = TilesRA.find { tile ->
							return tileVal == tile.value
						}
						def tileFile = getTileByName(tilesetMixFile, tile.name + theater.ext)

						// TODO: Create a single texture for each tile and re-use it but
						//       render it to a different place
						def tilePosW = cellNumberAsWorldCoords(tilePos)
						def tileImage = new Image(tileFile.width, tileFile.height, palette.format.value,
							tileFile.imagesData[tilePic].applyPalette(palette),
							new Rectanglef(tilePosW, new Vector2f(tilePosW).add(tileFile.width, tileFile.height)))
						tiles << tileImage
					}
				}
			}
		}
	}

	/**
	 * Convert a set of cell coordinates into a single cell value.
	 * 
	 * @param cellX
	 * @param cellY
	 * @return
	 */
	private static int cellCoordsAsNumber(int cellX, int cellY) {

		return cellY * TILES_VERTICAL + cellX
	}

	/**
	 * Convert a number representing a cell in a map into world coordinates.
	 * 
	 * @param cellNumber
	 * @return
	 */
	private static Vector2f cellNumberAsWorldCoords(int cellNumber) {

		def cellX = cellNumber % TILES_VERTICAL
		def cellY = TILES_VERTICAL - Math.ceil(cellNumber / TILES_HORIZONTAL)
		return new Vector2f(cellX * TILE_WIDTH, cellY * TILE_HEIGHT as float)
	}

	/**
	 * Retrieve the tile with the given name from the mix file.
	 * 
	 * @param mixFile
	 * @param tileName
	 * @return
	 */
	@Memoized
	private static TmpFileRA getTileByName(MixFile mixFile, String tileName) {

		return mixFile.getEntryData(mixFile.getEntry(tileName)).withStream { inputStream ->
			return new TmpFileRA(inputStream)
		}
	}

	/**
	 * Converts a map's character data into bytes that represent the tiles used
	 * throughout the map.
	 * 
	 * @param data
	 *   A map section containing the character data to decode.
	 * @return The converted map data.
	 */
	private static ByteBuffer mapDataToBytes(java.util.Map<String, String> data) {

		// Turn the section into 8-bit chars
		def sourceBytes = ByteBuffer.allocateNative(data.size() * 70) // Lines are only ever 70 characters long
		(1..data.size()).each { i ->
			def line = data[i.toString()]
			line.chars.each { c ->
				sourceBytes.put(c as byte)
			}
		}
		sourceBytes.flip()

		// Decode section bytes
		def mapBytes = ByteBuffer.allocateNative(49152) // 128x128x3 bytes max
		new PackData(6).decode(sourceBytes, mapBytes)
		return mapBytes
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		background.delete(renderer)
		tiles.each { tile ->
			tile.delete(renderer)
		}
	}

	@Override
	void init(GraphicsRenderer renderer) {

		background.init(renderer)
		tiles.each { tile ->
			tile.init(renderer)
		}
	}

	@Override
	void render(GraphicsRenderer renderer) {

		background.render(renderer)
		tiles.each { tile ->
			tile.render(renderer)
		}
	}

	/**
	 * Return some information about this map.
	 * 
	 * @return Map info.
	 */
	@Override
	String toString() {

		return "Name: ${name}, Theater: ${theater}"
	}
}
