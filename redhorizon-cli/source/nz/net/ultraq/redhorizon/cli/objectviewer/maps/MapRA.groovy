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

package nz.net.ultraq.redhorizon.cli.objectviewer.maps

import nz.net.ultraq.redhorizon.classic.codecs.PackData
import nz.net.ultraq.redhorizon.classic.filetypes.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileRA
import nz.net.ultraq.redhorizon.classic.units.Faction
import nz.net.ultraq.redhorizon.classic.units.Infantry
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.classic.units.Vehicle
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneElement
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneVisitor
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonSlurper
import java.nio.ByteBuffer

/**
 * A map on which a mission or a skirmish can take place.
 *
 * @author Emanuel Rabina
 */
class MapRA implements SceneElement<MapRA>, GraphicsElement {

	private static final Logger logger = LoggerFactory.getLogger(MapRA)

	private static final int TILES_X = 128
	private static final int TILES_Y = 128
	private static final int TILE_WIDTH = 24
	private static final int TILE_HEIGHT = 24

	static final Vector2f WORLD_OFFSET = new Vector2f(
		-TILES_X * TILE_WIDTH / 2,
		-TILES_Y * TILE_HEIGHT / 2
	)

	final String name
	final Theater theater
	final Rectanglef boundary
	final Vector2f initialPosition

	private final TileSet tileSet
	private final List<SceneElement> layers = []
	private Palette palette

	/**
	 * Construtor, build a map from the given map file.
	 *
	 * @param resourceManager
	 * @param mapFile
	 */
	MapRA(ResourceManager resourceManager, IniFile mapFile) {

		name = mapFile['Basic']['Name']

		def mapSection = mapFile['Map']
		def theaterString = mapSection['Theater']
		theater = Theater.valueOf(theaterString)
		palette = getResourceAsStream("ra-${theater.label.toLowerCase()}.pal").withBufferedStream { inputStream ->
			return new PalFile(inputStream).withAlphaMask()
		}
		def mapXY = new Vector2f(mapSection['X'] as int, mapSection['Y'] as int)
		def mapWH = new Vector2f(mapXY).add(mapSection['Width'] as int, mapSection['Height'] as int)
		boundary = new Rectanglef(mapXY.asWorldCoords(), mapWH.asWorldCoords()).makeValid()

		def waypoints = mapFile['Waypoints']
		def waypoint98 = waypoints['98'] as int
		initialPosition = waypoint98.asCellCoords().asWorldCoords()

		tileSet = new TileSet(palette)

		// Build the various layers
		layers << new BackgroundLayer(resourceManager)
		layers << new MapRAMapPack(resourceManager, mapDataToBytes(mapFile['MapPack'], 6))
		layers << new MapRAOverlayPack(resourceManager, mapDataToBytes(mapFile['OverlayPack'], 2))
		layers << new MapRATerrain(resourceManager, mapFile['TERRAIN'])

		layers << new MapRAUnits(resourceManager, mapFile['UNITS'])
		layers << new MapRAInfantry(resourceManager, mapFile['INFANTRY'])

		def halfMapWidth = (TILES_X * TILE_WIDTH) / 2 as float
		def halfMapHeight = (TILES_Y * TILE_HEIGHT) / 2 as float
		bounds.set(-halfMapWidth, -halfMapHeight, halfMapWidth, halfMapHeight)
		layers.each { layer ->
			layer.bounds.set(bounds)
		}
	}

	@Override
	void accept(SceneVisitor visitor) {

		visitor.visit(this)
		layers*.accept(visitor)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		tileSet.delete(renderer)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		tileSet.init(renderer)
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
		return new PackData(chunks).decode(sourceBytes, ByteBuffer.allocateNative(49152)) // 128x128x3 bytes max
	}

	@Override
	void render(GraphicsRenderer renderer) {
	}

	/**
	 * Return some information about this map.
	 *
	 * @return Map info.
	 */
	@Override
	String toString() {

		return """
			Red Alert map
			 - Name: ${name}
			 - Theater: ${theater}
			 - Bounds: x=${boundary.minX},y=${boundary.minY},w=${boundary.lengthX()},h=${boundary.lengthY()}
		""".stripIndent()
	}

	/**
	 * Special layer for the background image.
	 */
	private class BackgroundLayer implements SceneElement<BackgroundLayer> {

		private final MapBackground background

		/**
		 * Constructor, create the background image layer.
		 *
		 * @param resourceManager
		 */
		private BackgroundLayer(ResourceManager resourceManager) {

			def clearTileName = MapRAMapPackTile.DEFAULT.name + theater.ext
			def tileFile = resourceManager.loadFile(clearTileName, TmpFileRA)

			// Use the background tile to create a 5x4 repeating image
			def imageData = tileFile.imagesData
				.combineImages(tileFile.width, tileFile.height, theater.clearX)
			def width = tileFile.width * theater.clearX
			def height = tileFile.height * theater.clearY
			def repeatX = (TILES_X * TILE_WIDTH) / width as float
			def repeatY = (TILES_Y * TILE_HEIGHT) / height as float

			background = new MapBackground(width, height, imageData, repeatX, repeatY, palette)
				.translate(WORLD_OFFSET.x, WORLD_OFFSET.y, 0)
		}

		@Override
		void accept(SceneVisitor visitor) {

			visitor.visit(this)
			visitor.visit(background)
		}
	}

	/**
	 * Common code for rendering a map layer consisting of multiple elements.
	 */
	private abstract class MapLayer implements SceneElement<MapLayer> {

		protected final List<SceneElement> elements = []

		@Override
		void accept(SceneVisitor visitor) {

			visitor.visit(this)
			elements.each { element ->
				visitor.visit(element)
			}
		}
	}

	/**
	 * The "MapPack" layer of a Red Alert map.
	 */
	private class MapRAMapPack extends MapLayer implements GraphicsElement {

		private Mesh mesh
		private Material material

		/**
		 * Constructor, build the MapPack layer from the given tile data.
		 *
		 * @param resourceManager
		 * @param tileData
		 */
		MapRAMapPack(ResourceManager resourceManager, ByteBuffer tileData) {

			TILES_X.times { y ->
				TILES_Y.times { x ->

					// Get the byte representing the tile
					def tileValOffset = y * TILES_Y + x
					def tileVal = tileData.getShort(tileValOffset * 2)
					def tilePic = tileData.get(TILES_X * TILES_Y * 2 + tileValOffset)

					// Retrieve the appropriate tile, skip empty tiles
					if (tileVal != 0xff && tileVal != -1) {
						def tile = MapRAMapPackTile.find { tile ->
							return tile.value == tileVal
						}
						// Some unknown tile types still coming through?
						if (!tile) {
							logger.warn("Skipping unknown mappack tile type: ${tileVal}")
							return
						}
						def tileFile = resourceManager.loadFile(tile.name + theater.ext, TmpFileRA)

						// Skip references to invalid tiles
						if (tilePic >= tileFile.imagesData.length) {
							return
						}

						tileSet.addTiles(tileFile)
						elements << new MapElement(tileSet, tileFile, tilePic)
							.translate(new Vector2f(x, y).asWorldCoords(1))
					}
				}
			}
		}

		@Override
		void accept(SceneVisitor visitor) {

			visitor.visit(this)
		}

		@Override
		void delete(GraphicsRenderer renderer) {

			renderer.deleteMaterial(material)
		}

		@Override
		void init(GraphicsRenderer renderer) {

			(mesh, material) = renderer.withMaterialBundler { bundler ->
				elements.each { element ->
					if (element instanceof GraphicsElement) {
						element.init(bundler)
					}
				}
			}
		}

		@Override
		void render(GraphicsRenderer renderer) {

			renderer.draw(
				mesh: mesh,
				material: material
			)
		}
	}

	/**
	 * The "OverlayPack" layer of a Red Alert map.
	 */
	private class MapRAOverlayPack extends MapLayer {

		/**
		 * Constructor, build the OverlayPack layer from the given tile data.
		 *
		 * @param resourceManager
		 * @param tileData
		 */
		MapRAOverlayPack(ResourceManager resourceManager, ByteBuffer tileData) {

			Map<Vector2f, MapRAOverlayPackTile> tileTypes = [:]

			TILES_X.times { y ->
				TILES_Y.times { x ->

					// Get the byte representing the tile
					def tileVal = tileData.get()

					// Retrieve the appropriate tile, skip empty tiles
					if (tileVal != -1) {
						def tile = MapRAOverlayPackTile.find { tile ->
							return tile.value == tileVal
						}
						// Some unknown tile types still coming through?
						if (!tile) {
							logger.warn("Skipping unknown overlay tile type: ${tileVal}")
							return
						}
						tileTypes << [(new Vector2f(x, y)): tile]
					}
				}
			}

			// Now that the tiles are all assembled, build the appropriate image
			// representation for them
			tileTypes.each { tilePos, tile ->
				def tileFile = tile.isWall || tile.useShp ?
					resourceManager.loadFile("${tile.name}.shp", ShpFile) :
					resourceManager.loadFile(tile.name + theater.ext, ShpFile)
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

				tileSet.addTiles(tileFile)
				elements << new MapElement(tileSet, tileFile, imageVariant)
					.translate(new Vector2f(tilePos).asWorldCoords(1))
			}
		}
	}

	/**
	 * The "Terrain" layer of a Red Alert map.
	 */
	private class MapRATerrain extends MapLayer {

		/**
		 * Constructor, build the terrain layer from the given terrain data.
		 *
		 * @param resourceManager
		 * @param terrainData
		 */
		MapRATerrain(ResourceManager resourceManager, Map<String, String> terrainData) {

			terrainData.each { cell, terrainType ->
				def terrainFile = resourceManager.loadFile(terrainType + theater.ext, ShpFile)
				def cellPosXY = (cell as int).asCellCoords().asWorldCoords(terrainFile.height / TILE_HEIGHT - 1 as int)
//				def cellPosWH = new Vector2f(cellPosXY).add(terrainFile.width, terrainFile.height)
				tileSet.addTiles(terrainFile)
				elements << new MapElement(tileSet, terrainFile, 0)
					.translate(cellPosXY)
			}

			// Sort the terrain elements so that ones lower down the map render "over"
			// those higher up the map
//			elements.sort { imageA, imageB ->
//				return imageB.dimensions.maxX - imageA.dimensions.maxX ?:
//				       imageB.dimensions.minX - imageA.dimensions.minX
//			}
		}
	}

	/**
	 * The "Units" layer of a map.
	 */
	private class MapRAUnits extends MapLayer {

		/**
		 * Constructor, build the unit layer from the given unit data.
		 *
		 * @param resourceManager
		 * @param unitData
		 */
		MapRAUnits(ResourceManager resourceManager, Map<String, String> unitData) {

			unitData.each { index, data ->
				var unitLine = UnitLine.fromString(data)

				// TODO: Add resource path support to the resource manager
				var unitConfig = getResourceAsStream("nz/net/ultraq/redhorizon/classic/units/data/${unitLine.type.toLowerCase()}.json")
					.withBufferedStream { inputStream ->
						return new JsonSlurper().parseText(inputStream.text) as UnitData
					}
				var unitImages = resourceManager.loadFile("${unitLine.type}.shp", ShpFile)

				var unit = new Vehicle(unitConfig, unitImages, palette)

				// TODO: Country to faction map
				unit.faction = switch (unitLine.faction) {
					case "Greece" -> Faction.BLUE
					case "USSR" -> Faction.RED
					default -> Faction.GOLD
				}

				unit.heading = unitLine.heading
				unit.translate(unitLine.coords.asWorldCoords())

				elements << unit
			}
		}
	}

	/**
	 * The "Infantry" layer of a map.
	 */
	private class MapRAInfantry extends MapLayer {

		/**
		 * Constructor, build the infantry layer from the given infantry data.
		 *
		 * @param resourceManager
		 * @param infantryData
		 */
		MapRAInfantry(ResourceManager resourceManager, Map<String, String> infantryData) {

			var jsonSlurper = new JsonSlurper()
			infantryData.each { index, data ->
				var infantryLine = InfantryLine.fromString(data)

				try {

					// TODO: Add resource path support to the resource manager
					var unitConfigJson = getResourceAsText("nz/net/ultraq/redhorizon/classic/units/data/${infantryLine.type.toLowerCase()}.json")
					var unitConfig = jsonSlurper.parseText(unitConfigJson) as UnitData
					var infantryImages = resourceManager.loadFile("${infantryLine.type}.shp", ShpFile)

					var infantry = new Infantry(unitConfig, infantryImages, palette).tap { it ->

						// TODO: Country to faction map
						it.faction = switch (infantryLine.faction) {
							case "Greece" -> Faction.BLUE
							case "USSR" -> Faction.RED
							default -> Faction.GOLD
						}
						it.heading = infantryLine.heading

						// TODO: Sub positions within cells
						it.translate(infantryLine.coords.asWorldCoords())
					}

					elements << infantry
				}
				catch (IllegalArgumentException ignored) {
					// Ignore unknown units
					logger.warn("Unhandled unit line encountered, line ${index}, data: ${data}")
				}
			}
		}
	}
}
