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
import nz.net.ultraq.redhorizon.classic.filetypes.shp.ShpFile
import nz.net.ultraq.redhorizon.classic.filetypes.tmp.TmpFileRA
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Rectanglef
import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.Memoized
import java.nio.ByteBuffer

/**
 * A map on which a mission or a skirmish can take place.
 * 
 * @author Emanuel Rabina
 */
class MapRA implements GraphicsElement, SelfVisitable {

	private static final Logger logger = LoggerFactory.getLogger(MapRA)

	private static final int TILES_X = 128
	private static final int TILES_Y = 128
	private static final int TILE_WIDTH = 24
	private static final int TILE_HEIGHT = 24

	private static final Vector2f X_AXIS_MIN = new Vector2f(-3600, 0)
	private static final Vector2f X_AXIS_MAX = new Vector2f(3600, 0)
	private static final Vector2f Y_AXIS_MIN = new Vector2f(0, -3600)
	private static final Vector2f Y_AXIS_MAX = new Vector2f(0, 3600)

	final String name
	final Theaters theater
	final Rectanglef boundary
	final Vector2f[] boundaryPoints
	final Vector2f initialPosition

	private Image background
	private MapRAMapPack mapPackLayer
	private MapRAOverlayPack overlayPackLayer

	/**
	 * Construtor, build a map from the given map file.
	 * 
	 * @param mapFile
	 */
	MapRA(IniFile mapFile) {

		name = mapFile['Basic']['Name']

		def mapSection = mapFile['Map']
		def theaterString = mapSection['Theater']
		theater = Theaters.find { theater ->
			return theater.label.equalsIgnoreCase(theaterString)
		}
		def palette = getResourceAsStream("ra-${theater.label.toLowerCase()}.pal").withBufferedStream { inputStream ->
			return new PalFile(inputStream).withAlphaMask()
		}
		def mapXY = new Vector2f(mapSection['X'] as int, mapSection['Y'] as int)
		def mapWH = new Vector2f(mapXY).add(mapSection['Width'] as int, mapSection['Height'] as int)
		boundary = new Rectanglef(mapXY.asWorldCoords(), mapWH.asWorldCoords()).switchY()
		boundaryPoints = boundary.asPoints()

		def waypoints = mapFile['Waypoints']
		def waypoint98 = waypoints['98'] as int
		initialPosition = waypoint98.asCellCoords().asWorldCoords()

		// TODO: Figure out some way to share knowledge of the path to mix files
		//       containing the necessary files
		new MixFile(new File("mix/red-alert/MapTiles_${theater.label}.mix")).withCloseable { tilesMixFile ->
			def clearTileName = MapRAMapPackTiles.DEFAULT.name + theater.ext
			def backgroundTileFile = tilesMixFile.getEntryData(tilesMixFile.getEntry(clearTileName)).withBufferedStream { inputStream ->
				return new TmpFileRA(inputStream)
			}

			// Use the background tile to create a 5x4 repeating image
			def combinedBackgroundData = backgroundTileFile.imagesData
				.combineImages(backgroundTileFile.width, backgroundTileFile.height, theater.clearX)
				.applyPalette(palette)
			def combinedWidth = backgroundTileFile.width * theater.clearX
			def combinedHeight = backgroundTileFile.height * theater.clearY
			background = new Image(combinedWidth, combinedHeight, palette.format.value, combinedBackgroundData,
				new Rectanglef(0, 0, TILES_X * TILE_WIDTH, TILES_Y * TILE_HEIGHT),
				TILES_X * TILE_WIDTH / combinedWidth, TILES_Y * TILE_HEIGHT / combinedHeight
			)

			// Build the various layers
			mapPackLayer = new MapRAMapPack(tilesMixFile, palette, mapDataToBytes(mapFile['MapPack'], 6))
			overlayPackLayer = new MapRAOverlayPack(tilesMixFile, palette, mapDataToBytes(mapFile['OverlayPack'], 2))
		}
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		background.delete(renderer)
		mapPackLayer.delete(renderer)
		overlayPackLayer.delete(renderer)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		background.init(renderer)
		mapPackLayer.init(renderer)
		overlayPackLayer.init(renderer)
	}

	/**
	 * Converts a map's character data into bytes that represent the tiles used
	 * throughout the map.
	 * 
	 * @param data
	 *   A map section containing the character data to decode.
	 * @param chunks
	 *   Number of chunks to allocate the pack data during the decoding process.
	 * @return The converted map data.
	 */
	private static ByteBuffer mapDataToBytes(Map<String, String> data, int chunks) {

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
		new PackData(chunks).decode(sourceBytes, mapBytes)
		return mapBytes
	}

	@Override
	void render(GraphicsRenderer renderer) {

		background.render(renderer)
		mapPackLayer.render(renderer)
		overlayPackLayer.render(renderer)
		renderer.drawLines(Colour.RED, X_AXIS_MIN, X_AXIS_MAX, Y_AXIS_MIN, Y_AXIS_MAX)
		renderer.drawLineLoop(Colour.YELLOW, boundaryPoints)
	}

	/**
	 * Return some information about this map.
	 * 
	 * @return Map info.
	 */
	@Override
	String toString() {

		return """
			Name: ${name}
			Theater: ${theater}
			Bounds: x=${boundary.minX},y=${boundary.minY},w=${boundary.lengthX()},h=${boundary.lengthY()}
		""".stripIndent()
	}

	/**
	 * The "MapPack" layer of a Red Alert map.
	 */
	private class MapRAMapPack implements GraphicsElement {

		final List<Image> tiles = []

		/**
		 * Constructor, build the MapPack layer from the given tile data.
		 * 
		 * @param tileMixFile
		 * @param tileData
		 */
		MapRAMapPack(MixFile tileMixFile, Palette palette, ByteBuffer tileData) {

			TILES_X.times { y ->
				TILES_Y.times { x ->
					// The +1 shift is to make up for the difference in coordinate systems
					def tileCoord = new Vector2f(x, y + 1)

					// Get the byte representing the tile
					def tileValOffset = y * TILES_Y + x
					def tileVal = tileData.getShort(tileValOffset * 2)
					def tilePic = tileData.get(TILES_X * TILES_Y * 2 + tileValOffset)

					// Retrieve the appropriate tile, skip empty tiles
					if (tileVal != 0xff && tileVal != -1) {
						def tile = MapRAMapPackTiles.find { tile ->
							return tile.value == tileVal
						}
						// Some unknown tile types still coming through?
						if (!tile) {
							logger.warn("Skipping unknown tile type: ${tileVal}")
							return
						}
						def tileFile = getTileByName(tileMixFile, tile.name + theater.ext)

						// TODO: Create a single texture for each tile and re-use it but
						//       render it to a different place
						def tilePos = new Vector2f(tileCoord).asWorldCoords()
						def tileImage = new Image(tileFile.width, tileFile.height, palette.format.value,
							tileFile.imagesData[tilePic].applyPalette(palette),
							new Rectanglef(tilePos, new Vector2f(tilePos).add(tileFile.width, tileFile.height)))
						tiles << tileImage
					}
				}
			}
		}

		@Override
		void delete(GraphicsRenderer renderer) {

			tiles.each { tile ->
				tile.delete(renderer)
			}
		}

		/**
		 * Retrieve the tile with the given name from the mix file as a Red Alert
		 * tile file.
		 * 
		 * @param mixFile
		 * @param tileName
		 * @return
		 */
		@Memoized
		private static TmpFileRA getTileByName(MixFile mixFile, String tileName) {

			return mixFile.getEntryData(mixFile.getEntry(tileName)).withBufferedStream { inputStream ->
				return new TmpFileRA(inputStream)
			}
		}

		@Override
		void init(GraphicsRenderer renderer) {

			tiles.each { tile ->
				tile.init(renderer)
			}
		}

		@Override
		void render(GraphicsRenderer renderer) {

			tiles.each { tile ->
				tile.render(renderer)
			}
		}
	}

	/**
	 * The "OverlayPack" layer of a Red Alert map.
	 */
	private class MapRAOverlayPack implements GraphicsElement {

		final List<Image> tiles = []

		/**
		 * Constructor, build the OverlayPack layer from the given tile data.
		 * 
		 * @param tileMixFile
		 * @param palette
		 * @param tileData
		 */
		MapRAOverlayPack(MixFile tileMixFile, Palette palette, ByteBuffer tileData) {

			Map<Vector2f,MapRAOverlayPackTiles> tileTypes = [:]

			// TODO: Figure out some way to share knowledge of the path to mix files
			//       containing the necessary files
			new MixFile(new File('mix/red-alert/Conquer.mix')).withCloseable { conquerMixFile ->
				TILES_X.times { y ->
					TILES_Y.times { x ->
						// The +1 shift is to make up for the difference in coordinate systems
						def tileCoord = new Vector2f(x, y + 1)

						// Get the byte representing the tile
						def tileVal = tileData.get()

						// Retrieve the appropriate tile, skip empty tiles
						if (tileVal != -1) {
							def tile = MapRAOverlayPackTiles.find { tile ->
								return tile.value == tileVal
							}
							tileTypes << [(tileCoord): tile]
						}
					}
				}

				// Now that the tiles are all assembled, build the appropriate image
				// representation for them
				tileTypes.each { tilePos, tile ->
					def tileFile = tile.isWall || tile.useShp ?
						getTileByName(conquerMixFile, "${tile.name}.shp") :
						getTileByName(tileMixFile, tile.name + theater.ext)
					def imageVariant = 0

					// Select the proper orientation for wall tiles
					if (tile.isWall) {
						if (tileTypes[new Vector2f(tilePos).add(0, -1)] == tile) {
							imageVariant |= 0x01
						}
						if (tileTypes[new Vector2f(tilePos).add(1, 0)] == tile) {
							imageVariant |= 0x02
						}
						if (tileTypes[new Vector2f(tilePos).add(0, 1)] == tile) {
							imageVariant |= 0x04
						}
						if (tileTypes[new Vector2f(tilePos).add(-1, 0)] == tile) {
							imageVariant |= 0x08
						}
					}
					// Select the proper density for resources
					else if (tile.isResource) {
						def adjacent = (-1..1).inject(0) { accY, y ->
							return accY + (-1..1).inject(0) { accX, x ->
								if (x != 0 && y != 0 && tileTypes[new Vector2f(tilePos).add(x, y)]?.isResource) {
									return accX + 1
								}
								return accX
							}
						}
						imageVariant = tile.name().startsWith('GEM') ?
							adjacent / 3 as int :
							3 + adjacent
					}

					// TODO: Create a single texture for each tile and re-use it but
					//       render it to a different place
					def tilePosW = new Vector2f(tilePos).asWorldCoords()
					def tileImage = new Image(tileFile.width, tileFile.height, palette.format.value,
						tileFile.imagesData[imageVariant].applyPalette(palette),
						new Rectanglef(tilePosW, new Vector2f(tilePosW).add(tileFile.width, tileFile.height)))
					tiles << tileImage
				}
			}
		}

		@Override
		void delete(GraphicsRenderer renderer) {

			tiles.each { tile ->
				tile.delete(renderer)
			}
		}

		/**
		 * Retrieve the tile with the given name from the mix file as a SHP file.
		 * 
		 * @param mixFile
		 * @param tileName
		 * @return
		 */
		@Memoized
		private static ShpFile getTileByName(MixFile mixFile, String tileName) {

			return mixFile.getEntryData(mixFile.getEntry(tileName)).withBufferedStream { inputStream ->
				return new ShpFile(inputStream)
			}
		}

		@Override
		void init(GraphicsRenderer renderer) {

			tiles.each { tile ->
				tile.init(renderer)
			}
		}

		@Override
		void render(GraphicsRenderer renderer) {

			tiles.each { tile ->
				tile.render(renderer)
			}
		}
	}
}
