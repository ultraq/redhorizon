/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.filetypes.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.MapFile
import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.RulesFile
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileRA
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.shaders.Shaders
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.graphics.Attribute
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.MeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Primitive
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonSlurper
import groovy.transform.ImmutableOptions
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

/**
 * A map on which a mission or skirmish can take place.
 *
 * @author Emanuel Rabina
 */
class Map extends Node<Map> {

	private static final Logger logger = LoggerFactory.getLogger(Map)
	private static final int TILES_X = 128
	private static final int TILES_Y = 128
	private static final int TILE_WIDTH = 24
	private static final int TILE_HEIGHT = 24

	final MapFile mapFile
	final String name = "Map - ${mapFile.basicSection.name()}"
	final Theater theater
	final Rectanglef boundary
	final Vector2f initialPosition

	private final Palette palette
	private final TileSet tileSet
	private final ResourceManager resourceManager
	private final RulesFile rules

	private CompletableFuture<Texture> paletteAsTextureFuture
	private Texture paletteAsTexture
	private CompletableFuture<SpriteSheet> tileSetSpriteSheetFuture

	/**
	 * Constructor, create a new map from a map file.
	 */
	Map(MapFile mapFile, ResourceManager resourceManager) {

		this.mapFile = mapFile
		this.resourceManager = resourceManager

		var mapSection = mapFile.mapSection
		theater = Theater.valueOf(mapSection.theater())

		// TODO: Include more palettes in the project, or load these via the resource manager
		palette = getResourceAsStream("ra-${theater.label.toLowerCase()}.pal").withBufferedStream { inputStream ->
			return new PalFile(inputStream)
		}

		var mapXY = new Vector2f(mapSection.x(), mapSection.y())
		var mapWH = new Vector2f(mapXY).add(mapSection.width(), mapSection.height())
		boundary = new Rectanglef(mapXY.asWorldCoords(), mapWH.asWorldCoords()).makeValid()

		var waypoints = mapFile.waypointsData
		var waypoint98 = waypoints[98]
		initialPosition = waypoint98.asCellCoords().asWorldCoords(1)

		var halfMapWidth = (TILES_X * TILE_WIDTH) / 2 as float
		var halfMapHeight = (TILES_Y * TILE_HEIGHT) / 2 as float
		bounds.set(-halfMapWidth, -halfMapHeight, halfMapWidth, halfMapHeight)

		tileSet = new TileSet()

		// Rules file needed for some object configuration
		var rulesIni = resourceManager.loadFile('rules.ini', IniFile)
		rules = rulesIni as RulesFile

		addChild(new MapBackground())
		addChild(new MapPack())
		addChild(new OverlayPack())

		// The following should be rendered in top-left to bottom-right order so
		// that "lower" objects get drawn over the "higher" ones
		addChild(new Terrain())
		addChild(new Structures())
		addChild(new Units())
		addChild(new Infantry())

		addChild(new MapLines())
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		tileSetSpriteSheetFuture = CompletableFuture.supplyAsync { ->
			return tileSet.tileFileList
				.collect { tileFile -> tileFile.imagesData }
				.flatten() as ByteBuffer[]
		}
			.thenComposeAsync { allTileImageData ->
				return scene
					.requestCreateOrGet(new SpriteSheetRequest(TILE_WIDTH, TILE_HEIGHT, ColourFormat.FORMAT_INDEXED, allTileImageData))
					.thenApplyAsync { newSpriteSheet ->
						tileSet.spriteSheet = newSpriteSheet
						return newSpriteSheet
					}
			}
		paletteAsTextureFuture = scene
			.requestCreateOrGet(new TextureRequest(256, 1, palette.format, palette as ByteBuffer))
			.thenApplyAsync { newTexture ->
				paletteAsTexture = newTexture
				return newTexture
			}

		return CompletableFuture.allOf(tileSetSpriteSheetFuture, paletteAsTextureFuture)
	}

	@Override
	CompletableFuture<Void> onSceneRemoved(Scene scene) {

		return scene.requestDelete(tileSet.spriteSheet, paletteAsTexture)
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
			 - Name: ${mapFile.basicSection.name()}
			 - Theater: ${theater}
			 - Bounds: x=${boundary.minX},y=${boundary.minY},w=${boundary.lengthX()},h=${boundary.lengthY()}
		""".stripIndent()
	}

	/**
	 * A specialized spritesheet that holds all of the tiles used in the map.
	 */
	private class TileSet {

		private final List<ImagesFile> tileFileList = []
		private final java.util.Map<ImagesFile, Integer> tileFileMap = [:]
		private int numTiles = 0
		private SpriteSheet spriteSheet

		/**
		 * Add more tiles to the tileset.
		 */
		void addTiles(ImagesFile tilesFile) {

			if (!tileFileList.contains(tilesFile)) {
				tileFileList << tilesFile
				tileFileMap << [(tilesFile): numTiles]
				numTiles += tilesFile.numImages
			}
		}

		/**
		 * Return the index into the spritesheet for the given tile file and frame.
		 */
		int getFrame(ImagesFile tileFile, int tileIndex) {

			return tileFileMap[tileFile] + tileIndex
		}
	}

	/**
	 * Map overlays and lines to help with debugging maps.
	 */
	private class MapLines extends Node<MapLines> {

		private static final Vector2f X_AXIS_MIN = new Vector2f(-3072, 0)
		private static final Vector2f X_AXIS_MAX = new Vector2f(3072, 0)
		private static final Vector2f Y_AXIS_MIN = new Vector2f(0, -3072)
		private static final Vector2f Y_AXIS_MAX = new Vector2f(0, 3072)

		MapLines() {

			addChild(new Primitive(MeshType.LINES, Colour.RED.withAlpha(0.8), X_AXIS_MIN, X_AXIS_MAX, Y_AXIS_MIN, Y_AXIS_MAX).tap {
				name = "XY axis (red)"
			})
			addChild(new Primitive(MeshType.LINE_LOOP, Colour.YELLOW.withAlpha(0.8), boundary as Vector2f[]).tap {
				name = "Map boundary (yellow)"
			})
		}
	}

	/**
	 * Special layer for the background image.
	 */
	private class MapBackground extends Node<MapBackground> {

		String name = "MapBackground - ${theater.label}"

		MapBackground() {

			var clearTileName = MapRAMapPackTile.DEFAULT.name + theater.ext
			var tileFile = resourceManager.loadFile(clearTileName, TmpFileRA)

			// Create a sprite sheet for the 5x4 background tile
			var imageData = tileFile.imagesData.combineImages(tileFile.width, tileFile.height, tileFile.format, theater.clearX)
			var spriteWidth = tileFile.width * theater.clearX
			var spriteHeight = tileFile.height * theater.clearY

			// Use the sprite sheet and repeat it over the entire map area
			var backgroundWidth = boundary.lengthX()
			var backgroundHeight = boundary.lengthY()
			var repeatX = backgroundWidth / spriteWidth as float
			var repeatY = backgroundHeight / spriteHeight as float

			var backgroundSprite = new PalettedSprite(backgroundWidth, backgroundHeight, tileFile.numImages, repeatX, repeatY, palette, { scene ->
				return scene.requestCreateOrGet(new SpriteSheetRequest(spriteWidth, spriteHeight, tileFile.format, imageData))
			})
			backgroundSprite.transform.translate(boundary.minX, boundary.minY)
			addChild(backgroundSprite)
		}
	}

	/**
	 * Representation of a single tile on the map, used for combining with other
	 * tiles to create a single large mesh for rendering a whole layer in a single
	 * draw call.
	 */
	@ImmutableOptions(
		// Not really an immutable class, but should be OK since we control its use within this file
		knownImmutables = ['position']
	)
	private static record MapTile(Vector2f position, int frameInTileSet) {}

	/**
	 * Common class for the *Pack layers of the map, which use 24x24 map tiles and
	 * can be optimized with a spritesheet.
	 */
	private abstract class Pack<T extends Pack> extends Node<T> implements GraphicsElement {

		protected final List<MapTile> mapTiles = []
		protected Mesh fullMesh
		protected Shader shader
		protected Material material = new Material()

		@Override
		CompletableFuture<Void> onSceneAdded(Scene scene) {

			return CompletableFuture.allOf(
				CompletableFuture.supplyAsync { ->
					List<Vector2f> allVertices = []
					List<Vector2f> allTextureUVs = []
					List<Integer> allIndices = []
					var indexOffset = 0
					mapTiles.each { mapTile ->
						allVertices.addAll(new Rectanglef(0, 0, TILE_WIDTH, TILE_HEIGHT).translate(mapTile.position()) as Vector2f[])
						allTextureUVs.addAll(tileSet.spriteSheet[mapTile.frameInTileSet()] as Vector2f[])
						allIndices.addAll([0, 1, 3, 1, 2, 3].collect { index -> index + indexOffset })
						indexOffset += 4
					}
					return new Tuple3<Vector2f[], Vector2f[], int[]>(allVertices as Vector2f[], allTextureUVs as Vector2f[], allIndices as int[])
				}
					.thenAcceptAsync { meshData ->
						def (allVertices, allTextureUVs, allIndices) = meshData
						return scene
							.requestCreateOrGet(new MeshRequest(MeshType.TRIANGLES,
								new VertexBufferLayout(Attribute.POSITION, Attribute.COLOUR, Attribute.TEXTURE_UVS),
								allVertices, Colour.WHITE, allTextureUVs, allIndices, false))
							.thenAcceptAsync { newMesh ->
								fullMesh = newMesh
							}
					},
				scene
					.requestCreateOrGet(new ShaderRequest(Shaders.palettedSpriteShader))
					.thenAcceptAsync { requestedShader ->
						shader = requestedShader
					},
				tileSetSpriteSheetFuture.thenApplyAsync { spriteSheet ->
					material.texture = spriteSheet.texture
					return spriteSheet
				},
				paletteAsTextureFuture.thenApplyAsync { palette ->
					material.palette = palette
					return palette
				},
				// TODO: Load the alpha mask once.  I think for these 'once' objects, I
				//       need a global object and uniform that these shaders use, much
				//       like the camera
				CompletableFuture.supplyAsync { ->
					var alphaMaskData = ByteBuffer.allocateNative(1024)
					(0..255).each { i ->
						alphaMaskData.put(
							switch (i) {
								case 0 -> new byte[]{ 0, 0, 0, 0 }
								case 4 -> new byte[]{ 0, 0, 0, 0x7f }
								default -> new byte[]{ 0xff, 0xff, 0xff, 0xff }
							}
						)
					}
					return alphaMaskData.flip()
				}
					.thenComposeAsync { alphaMaskData ->
						return scene
							.requestCreateOrGet(new TextureRequest(256, 1, ColourFormat.FORMAT_RGBA, alphaMaskData))
							.thenAcceptAsync { newTexture ->
								material.alphaMask = newTexture
							}
					}
			)
		}

		@Override
		CompletableFuture<Void> onSceneRemoved(Scene scene) {

			return scene.requestDelete(fullMesh)
		}

		@Override
		void render(GraphicsRenderer renderer) {

			if (fullMesh && shader && material.texture && material.palette && material.alphaMask) {
				renderer.draw(fullMesh, globalTransform, shader, material)
			}
		}
	}

	/**
	 * The "MapPack" layer of a Red Alert map.
	 */
	private class MapPack extends Pack<MapPack> {

		MapPack() {

			var tileData = mapFile.mapPackData

			TILES_X.times { y ->
				TILES_Y.times { x ->

					// Get the byte representing the tile
					var tileValOffset = y * TILES_Y + x
					var tileVal = tileData.getShort(tileValOffset * 2)
					var tilePic = tileData.get(TILES_X * TILES_Y * 2 + tileValOffset) & 0xff

					// Retrieve the appropriate tile, skip empty and default tiles
					if (tileVal != 0xff && tileVal != -1 && tileVal != 0) {
						var tile = MapRAMapPackTile.values().find { tile -> tile.value == tileVal }

						// Some unknown tile types still coming through?
						if (!tile) {
							logger.warn('Skipping unknown mappack tile type: {}', tileVal)
							return
						}
						var tileFile = resourceManager.loadFile(tile.name + theater.ext, TmpFileRA)

						// Skip references to invalid tiles
						if (tilePic >= tileFile.numImages) {
							logger.warn('Skipping unknown mappack tile image: {} pic: {}', tileVal, tilePic)
							return
						}

						tileSet.addTiles(tileFile)
						var mapTile = new MapTile(new Vector2f(x, y).asWorldCoords(1), tileSet.getFrame(tileFile, tilePic))
						mapTiles << mapTile

						bounds.expand(
							mapTile.position().x,
							mapTile.position().y,
							mapTile.position().x + TILE_WIDTH as float,
							mapTile.position().y + TILE_HEIGHT as float
						)
					}
				}
			}
		}

		@Override
		String getName() {

			return "MapPack - ${mapTiles.size()} tiles"
		}
	}

	/**
	 * The "OverlayPack" layer of a Red Alert map.
	 */
	private class OverlayPack extends Pack<OverlayPack> {

		OverlayPack() {

			var tileData = mapFile.overlayPackData
			java.util.Map<Vector2f, MapRAOverlayPackTile> tileTypes = [:]

			TILES_X.times { y ->
				TILES_Y.times { x ->

					// Get the byte representing the tile
					var tileVal = tileData.get()

					// Retrieve the appropriate tile, skip empty tiles
					if (tileVal != -1) {
						var tile = MapRAOverlayPackTile.values().find { tile -> tile.value == tileVal }

						// TODO: The current spritesheet only works if all images are the
						//       same dimensions, but the CRATE item is 10x11 instead of
						//       24x24.  Ignore these for now, but find some way to support
						//       these odd image sizes
						if (tile in [MapRAOverlayPackTile.CRATE_WOOD, MapRAOverlayPackTile.CRATE_SILVER, MapRAOverlayPackTile.CRATE_WATER]) {
							logger.warn('Skipping over CRATE overlay due to differing dimensions')
							return
						}

						// Some unknown tile types still coming through?
						if (!tile) {
							logger.warn('Skipping unknown overlay tile type: {}', tileVal)
							return
						}

						tileTypes << [(new Vector2f(x, y)): tile]
					}
				}
			}

			// Now that the tiles are all assembled, build the appropriate image
			// representation for them
			tileTypes.each { tilePos, tile ->
				var tileFile = tile.isWall || tile.useShp ?
					resourceManager.loadFile("${tile.name}.shp", ShpFile) :
					resourceManager.loadFile(tile.name + theater.ext, ShpFile)
				var imageVariant = 0

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
					var adjacent = (-1..1).inject(0) { accY, y ->
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
				var mapTile = new MapTile(new Vector2f(tilePos).asWorldCoords(1), tileSet.getFrame(tileFile, imageVariant))
				mapTiles << mapTile

				bounds.expand(
					mapTile.position().x,
					mapTile.position().y,
					mapTile.position().x + TILE_WIDTH as float,
					mapTile.position().y + TILE_HEIGHT as float
				)
			}
		}

		@Override
		String getName() {

			return "OverlayPack - ${mapTiles.size()} tiles"
		}
	}

	/**
	 * The "Terrain" layer of a Red Alert map.
	 */
	private class Terrain extends Node<Terrain> {

		private final TileSet tileSet = new TileSet()
		private final List<MapTile> mapTiles = []

		Terrain() {

			var terrainData = mapFile.terrainData
			terrainData.each { cell, terrainType ->
				var terrainFile = resourceManager.loadFile(terrainType + theater.ext, ShpFile)
				var cellPosXY = (cell as int).asCellCoords().asWorldCoords(terrainFile.height / TILE_HEIGHT as int)
				tileSet.addTiles(terrainFile)

				// TODO: Get TileSets to work with any sized sprites, then terrain can
				//       be included in super map tileset
//				var mapTile = new MapTile(cellPosXY, tileSet.getFrame(terrainFile, 0))
//				mapTiles << mapTile

//				bounds.expand(
//					mapTile.position().x,
//					mapTile.position().y,
//					mapTile.position().x + TILE_WIDTH as float,
//					mapTile.position().y + TILE_HEIGHT as float
//				)

				addChild(new PalettedSprite(terrainFile, palette).tap {
					name = "Terrain ${terrainType}"
					transform.translate(cellPosXY)
				})
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
	 * Common class for the faction units/structures of the map.
	 */
	private abstract class FactionObjects<T extends FactionObjects, L extends ObjectLine> extends Node<T> {

		Unit createObject(L objectLine,
			@ClosureParams(value = FromString, options = 'nz.net.ultraq.redhorizon.classic.units.Unit, nz.net.ultraq.redhorizon.classic.units.UnitData')
				Closure configure) {

			var unitConfig = rules.getUnitConfig(objectLine.type)
			var unitImages = resourceManager.loadFile("${unitConfig.image ?: objectLine.type}.shp", ShpFile)
			var unitJson = getResourceAsText("nz/net/ultraq/redhorizon/classic/units/data/${objectLine.type.toLowerCase()}.json")
			var unitData = new JsonSlurper().parseText(unitJson) as UnitData

			return new Unit(unitImages, palette, unitData).tap {
				configure(it, unitData)

				// TODO: Country to faction map
				faction = switch (objectLine.faction) {
					case 'Greece' -> Faction.BLUE
					case 'England' -> Faction.GREEN
					case 'USSR' -> Faction.RED
					case 'Neutral' -> Faction.GOLD
					default -> {
						logger.warn("Unmapped country ${objectLine.faction}")
						yield Faction.GOLD
					}
				}
				heading = objectLine.heading
			}
		}
	}

	/**
	 * The "Units" section of a Red Alert map.
	 */
	private class Units extends FactionObjects<Units, UnitLine> {

		Units() {

			mapFile.unitsData.eachWithIndex { unitLine, index ->
				try {
					var unit = createObject(unitLine) { unit, unitData ->
						unit.name = "Vehicle - ${unitLine.faction}, ${unitLine.type}"
						unit.transform.translate((unitLine.coords as Vector2f)
							.asWorldCoords(1)
							.centerInCell(unit.width, unit.height))
					}
					addChild(unit)
				}
				catch (IllegalArgumentException ignored) {
					// Ignore unknown units
					logger.warn('Unhandled unit line encountered, line {}, type: {}', index, unitLine.type)
				}
			}
		}
	}

	/**
	 * The "Infantry" section of a Red Alert map.
	 */
	private class Infantry extends FactionObjects<Infantry, InfantryLine> {

		Infantry() {

			mapFile.infantryData.eachWithIndex { infantryLine, index ->
				try {
					var infantry = createObject(infantryLine) { infantry, infantryData ->
						infantry.name = "Infantry - ${infantryLine.faction}, ${infantryLine.type}"
						infantry.transform.translate((infantryLine.coords as Vector2f)
							.asWorldCoords(1)
							.centerInCell(infantry.width, infantry.height))
						switch (infantryLine.cellPos) {
							case 1 -> infantry.transform.translate(-8, 8)
							case 2 -> infantry.transform.translate(8, 8)
							case 3 -> infantry.transform.translate(-8, -8)
							case 4 -> infantry.transform.translate(8, -8)
						}
					}
					addChild(infantry)
				}
				catch (IllegalArgumentException ignored) {
					// Ignore unknown units
					logger.warn('Unhandled unit line encountered, line {}, type: {}', index, infantryLine.type)
				}
			}
		}
	}

	/**
	 * The "Structures" section of a Red Alert map.
	 */
	private class Structures extends FactionObjects<Structures, StructureLine> {

		Structures() {

			mapFile.structuresData.eachWithIndex { structureLine, index ->
				try {
					var structure = createObject(structureLine) { structure, structureData ->
						structure.name = "Structure - ${structureLine.faction}, ${structureLine.type}"
						var translate = (structureLine.coords as Vector2f).asWorldCoords(Math.ceil(structure.height / TILE_HEIGHT) as int)
						if (structure.width < TILE_WIDTH || structure.height < TILE_HEIGHT) {
							translate.centerInCell(structure.width, structure.height)
						}
						structure.transform.translate(translate)

						// A special case for structures with secondary parts, namely the
						// weapons factory for its garage door
						var combineWith = structureData.shpFile.parts.body.combineWith
						if (combineWith) {
							var combinedImages = resourceManager.loadFile("${combineWith}.shp", ShpFile)
							var combinedJson = getResourceAsText("nz/net/ultraq/redhorizon/classic/units/data/${combineWith.toLowerCase()}.json")
							var combinedData = new JsonSlurper().parseText(combinedJson) as UnitData
							structure.addBody(combinedImages, palette, combinedData)
						}

						// Structure bib if applicable
						var structureConfig = rules.getStructureConfig(structureLine.type)
						if (structureConfig.bib()) {
							var structureWidthInCells = Math.ceil(structure.width / TILE_WIDTH) as int
							var bib = switch (structureWidthInCells) {
								case 2 -> resourceManager.loadFile("bib3${theater.ext}", ShpFile)
								case 3 -> resourceManager.loadFile("bib2${theater.ext}", ShpFile)
								case 4 -> resourceManager.loadFile("bib1${theater.ext}", ShpFile)
								default -> null
							}
							if (bib) {
								var bibImageData = bib.imagesData.combineImages(bib.width, bib.height, bib.format, structureWidthInCells)
								var bibWidth = TILE_WIDTH * structureWidthInCells
								var bibHeight = TILE_HEIGHT * 2
								var bibSprite = new PalettedSprite(bibWidth, bibHeight, 1, palette, { scene ->
									return scene.requestCreateOrGet(new SpriteSheetRequest(bibWidth, bibHeight, ColourFormat.FORMAT_INDEXED, bibImageData))
								})
								bibSprite.name = "Bib"
								bibSprite.transform.translate(0, -TILE_HEIGHT)
								structure.addChild(0, bibSprite)
							}
						}
					}
					addChild(structure)
				}
				catch (IllegalArgumentException ignored) {
					// Ignore unknown units
					logger.warn('Unhandled unit line encountered, line {}, type: {}', index, structureLine.type)
				}
			}
		}
	}
}
