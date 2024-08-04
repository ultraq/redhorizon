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
import nz.net.ultraq.redhorizon.classic.filetypes.RulesFile
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileRA
import nz.net.ultraq.redhorizon.classic.nodes.Layer
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.resources.PalettedSpriteMaterial
import nz.net.ultraq.redhorizon.classic.shaders.Shaders
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.engine.graphics.Attribute
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.MeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Mesh.MeshType
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.NodeListDisplayHint
import nz.net.ultraq.redhorizon.engine.scenegraph.PartitionHint
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.UpdateHint
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Primitive
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

	static final int TILE_WIDTH = 24
	static final int TILE_HEIGHT = 24
	static final int TILES_X = 128
	static final int TILES_Y = 128
	static final Rectanglef MAX_BOUNDS = new Rectanglef(0, 0, TILES_X * TILE_WIDTH, TILES_Y * TILE_HEIGHT).center()

	private static final Logger logger = LoggerFactory.getLogger(Map)

	final MapFile mapFile
	final String name = "Map - ${mapFile.basicSection.name()}"
	final Theater theater
	final Rectanglef boundary
	final Vector2f initialPosition

	protected final Matrix4f transformCopy = new Matrix4f()
	protected final PalettedSpriteMaterial materialCopy = new PalettedSpriteMaterial()

	private final ResourceManager resourceManager
	private final RulesFile rules

	/**
	 * Constructor, create a new map from a map file.
	 */
	Map(MapFile mapFile, ResourceManager resourceManager) {

		this.mapFile = mapFile
		this.resourceManager = resourceManager

		var mapSection = mapFile.mapSection
		theater = Theater.valueOf(mapSection.theater())

		var mapXY = new Vector2f(mapSection.x(), mapSection.y())
		var mapWH = new Vector2f(mapXY).add(mapSection.width(), mapSection.height())
		boundary = new Rectanglef(mapXY.asWorldCoords(), mapWH.asWorldCoords()).makeValid()

		var waypoints = mapFile.waypointsData
		var waypoint98 = waypoints[98]
		initialPosition = waypoint98.asCellCoords().asWorldCoords(1)

		bounds { ->
			set(MAX_BOUNDS)
		}

		// Rules file needed for some object configuration
		var rulesIni = resourceManager.loadFile('rules.ini', IniFile)
		rules = rulesIni as RulesFile

		addChild(new MapBackground().tap {
			layer = Layer.MAP_BACKGROUND
		})
		addChild(new MapPack().tap {
			layer = Layer.MAP_PACK
		})
		addChild(new OverlayPack().tap {
			layer = Layer.OVERLAY_PACK
		})
		addChild(new Terrain().tap {
			layer = Layer.SPRITES
		})
		addChild(new Structures().tap {
			layer = Layer.SPRITES
		})
		addChild(new Units().tap {
			layer = Layer.SPRITES
		})
		addChild(new Infantry().tap {
			layer = Layer.SPRITES
		})

		addChild(new MapLines().tap {
			layer = Layer.OVERLAY
		})
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

			if (tilesFile.width > TILE_WIDTH || tilesFile.height > TILE_HEIGHT) {
				throw new IllegalArgumentException('Cannot use a tile file whose dimensions exceed 24x24')
			}

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

		final UpdateHint updateHint = UpdateHint.NEVER

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
		final PartitionHint partitionHint = PartitionHint.LARGE_AREA
		final UpdateHint updateHint = UpdateHint.NEVER

		private final PalettedSprite backgroundSprite

		MapBackground() {

			var clearTileName = MapRAMapPackTile.DEFAULT.name + theater.ext
			var tileFile = resourceManager.loadFile(clearTileName, TmpFileRA)

			// Create a sprite sheet for the 5x4 background tile
			var imageData = tileFile.imagesData.combine(tileFile.width, tileFile.height, tileFile.format, theater.clearX)
			var spriteWidth = tileFile.width * theater.clearX
			var spriteHeight = tileFile.height * theater.clearY

			// Use the sprite sheet and repeat it over the entire map area
			var backgroundWidth = boundary.lengthX()
			var backgroundHeight = boundary.lengthY()
			var repeatX = backgroundWidth / spriteWidth as float
			var repeatY = backgroundHeight / spriteHeight as float

			backgroundSprite = new PalettedSprite(backgroundWidth, backgroundHeight, tileFile.numImages, repeatX, repeatY, { scene ->
				return scene.requestCreateOrGet(new SpriteSheetRequest(spriteWidth, spriteHeight, tileFile.format, imageData))
			})
			backgroundSprite.setPosition(boundary.minX, boundary.minY)
			addChild(backgroundSprite)
		}
	}

	/**
	 * Representation of a single tile on the map, used for combining with other
	 * tiles to create a single large mesh for rendering a whole layer in a single
	 * draw call.
	 */
	@ImmutableOptions(knownImmutables = ['position'])
	private static record MapTile(Vector2f position, int frameInTileSet) {}

	/**
	 * The "MapPack" layer of a Red Alert map.
	 */
	private class MapPack extends Node<MapPack> implements GraphicsElement {

		final PartitionHint partitionHint = PartitionHint.LARGE_AREA
		final UpdateHint updateHint = UpdateHint.NEVER

		private final TileSet tileSet = new TileSet()
		private final List<MapTile> mapTiles = []
		private Mesh fullMesh
		private Shader shader
		private PalettedSpriteMaterial material = new PalettedSpriteMaterial()

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

						bounds { ->
							expand(
								mapTile.position().x,
								mapTile.position().y,
								mapTile.position().x + TILE_WIDTH as float,
								mapTile.position().y + TILE_HEIGHT as float
							)
						}
					}
				}
			}
		}

		@Override
		String getName() {

			return "MapPack - ${mapTiles.size()} tiles"
		}

		@Override
		CompletableFuture<Void> onSceneAddedAsync(Scene scene) {

			return CompletableFuture.allOf(
				CompletableFuture.supplyAsync { ->
					return tileSet.tileFileList
						.collect { tileFile ->
							return tileFile.imagesData.collect { imageData ->
								return imageData.center(tileFile.width, tileFile.height, TILE_WIDTH, TILE_HEIGHT)
							}
						}
						.flatten() as ByteBuffer[]
				}
					.thenComposeAsync { allTileImageData ->
						return scene.requestCreateOrGet(new SpriteSheetRequest(TILE_WIDTH, TILE_HEIGHT, ColourFormat.FORMAT_INDEXED, allTileImageData))
					}
					.thenApplyAsync { newSpriteSheet ->
						tileSet.spriteSheet = newSpriteSheet
						material.with {
							texture = newSpriteSheet.texture
							frame = 0
							frameStepX = newSpriteSheet.frameStepX
							frameStepY = newSpriteSheet.frameStepY
							framesHorizontal = newSpriteSheet.framesHorizontal
							framesVertical = newSpriteSheet.framesVertical
						}
						return newSpriteSheet
					}
					.thenApplyAsync { spriteSheet ->
						List<Vector2f> allVertices = []
						List<Vector2f> allTextureUVs = []
						List<Integer> allIndices = []
						var indexOffset = 0
						mapTiles.each { mapTile ->
							allVertices.addAll(new Rectanglef(0, 0, TILE_WIDTH, TILE_HEIGHT).translate(mapTile.position()) as Vector2f[])
							allTextureUVs.addAll(spriteSheet[mapTile.frameInTileSet()] as Vector2f[])
							allIndices.addAll([0, 1, 2, 0, 2, 3].collect { index -> index + indexOffset })
							indexOffset += 4
						}
						return new Tuple3<Vector2f[], Vector2f[], int[]>(allVertices as Vector2f[], allTextureUVs as Vector2f[], allIndices as int[])
					}
					.thenComposeAsync { meshData ->
						def (allVertices, allTextureUVs, allIndices) = meshData
						return scene
							.requestCreateOrGet(new MeshRequest(MeshType.TRIANGLES,
								new VertexBufferLayout(Attribute.POSITION, Attribute.COLOUR, Attribute.TEXTURE_UVS),
								allVertices, Colour.WHITE, allTextureUVs, allIndices))
							.thenAcceptAsync { newMesh ->
								fullMesh = newMesh
							}
					},
				scene
					.requestCreateOrGet(new ShaderRequest(Shaders.palettedSpriteShader))
					.thenAcceptAsync { requestedShader ->
						shader = requestedShader
					}
			)
		}

		@Override
		CompletableFuture<Void> onSceneRemovedAsync(Scene scene) {

			return scene.requestDelete(fullMesh, tileSet.spriteSheet)
		}

		@Override
		RenderCommand renderCommand() {

			transformCopy.set(globalTransform)
			materialCopy.copy(material)

			return { renderer ->
				if (fullMesh && shader && materialCopy.texture) {
					renderer.draw(fullMesh, transformCopy, shader, materialCopy)
				}
			}
		}
	}

	/**
	 * The "OverlayPack" layer of a Red Alert map.
	 */
	private class OverlayPack extends Node<OverlayPack> {

		final PartitionHint partitionHint = PartitionHint.DO_NOT_PARTICIPATE
		final NodeListDisplayHint nodeListDisplayHint = NodeListDisplayHint.START_COLLAPSED
		final UpdateHint updateHint = UpdateHint.NEVER

		private int numTiles = 0

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

				var overlay = new PalettedSprite(tileFile)
				overlay.name = "${tile.name} - Variant ${imageVariant}"
				overlay.frame = imageVariant
				overlay.position = new Vector2f(tilePos).asWorldCoords(1)
				overlay.partitionHint = PartitionHint.SMALL_AREA

				addChild(overlay)

				numTiles++
			}
		}

		@Override
		String getName() {

			return "OverlayPack - ${numTiles} tiles"
		}
	}

	/**
	 * The "Terrain" layer of a Red Alert map.
	 */
	private class Terrain extends Node<Terrain> {

		final PartitionHint partitionHint = PartitionHint.DO_NOT_PARTICIPATE
		final NodeListDisplayHint nodeListDisplayHint = NodeListDisplayHint.START_COLLAPSED
		final UpdateHint updateHint = UpdateHint.NEVER

		Terrain() {

			var terrainData = mapFile.terrainData
			terrainData.each { cell, terrainType ->
				var terrainFile = resourceManager.loadFile(terrainType + theater.ext, ShpFile)
				var cellPosXY = (cell as int).asCellCoords().asWorldCoords(terrainFile.height / TILE_HEIGHT as int)

				var terrain = new PalettedSprite(terrainFile)
				terrain.name = "Terrain ${terrainType}"
				terrain.position = cellPosXY
				terrain.partitionHint = PartitionHint.SMALL_AREA
				addChild(terrain)
			}
		}
	}

	/**
	 * Common class for the faction units/structures of the map.
	 */
	private abstract class FactionObjects<T extends FactionObjects, L extends ObjectLine> extends Node<T> {

		final PartitionHint partitionHint = PartitionHint.DO_NOT_PARTICIPATE

		Unit createObject(L objectLine,
			@ClosureParams(value = FromString, options = 'nz.net.ultraq.redhorizon.classic.units.Unit, nz.net.ultraq.redhorizon.classic.units.UnitData')
				Closure configure) {

			var unitConfig = rules.getUnitConfig(objectLine.type)
			var unitData = getUnitData(objectLine.type)
			var unitImages = resourceManager.loadFile("${unitConfig.image ?: objectLine.type}${unitData.spriteSource == 'theater' ? theater.ext : '.shp'}", ShpFile)

			return new Unit(unitImages, unitData).tap {
				configure(it, unitData)

				// TODO: Country to faction map
				faction = switch (objectLine.faction) {
					case 'Greece', 'GoodGuy' -> Faction.BLUE
					case 'Spain' -> Faction.GOLD
					case 'England' -> Faction.GREEN
					case 'Germany' -> Faction.BROWN
					case 'France' -> Faction.TEAL
					case 'Turkey' -> Faction.MAROON
					case 'USSR', 'BadGuy' -> Faction.RED
					case 'Ukraine' -> Faction.ORANGE
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

		final NodeListDisplayHint nodeListDisplayHint = NodeListDisplayHint.START_COLLAPSED

		Units() {

			mapFile.unitsData.eachWithIndex { unitLine, index ->
				try {
					var unit = createObject(unitLine) { unit, unitData ->
						unit.name = "Vehicle - ${unitLine.faction}, ${unitLine.type}"
						unit.position = (unitLine.coords as Vector2f)
							.asWorldCoords(1)
							.centerInCell(unit.width, unit.height)
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

		final NodeListDisplayHint nodeListDisplayHint = NodeListDisplayHint.START_COLLAPSED

		Infantry() {

			mapFile.infantryData.eachWithIndex { infantryLine, index ->
				try {
					var infantry = createObject(infantryLine) { infantry, infantryData ->
						infantry.name = "Infantry - ${infantryLine.faction}, ${infantryLine.type}"
						infantry.position = (infantryLine.coords as Vector2f)
							.asWorldCoords(1)
							.centerInCell(infantry.width, infantry.height)
						switch (infantryLine.cellPos) {
							case 1 -> infantry.transform { -> translate(-8, 8) }
							case 2 -> infantry.transform { -> translate(8, 8) }
							case 3 -> infantry.transform { -> translate(-8, -8) }
							case 4 -> infantry.transform { -> translate(8, -8) }
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

		final NodeListDisplayHint nodeListDisplayHint = NodeListDisplayHint.START_COLLAPSED

		Structures() {

			mapFile.structuresData.eachWithIndex { structureLine, index ->
				try {
					var structure = createObject(structureLine) { structure, structureData ->
						structure.name = "Structure - ${structureLine.faction}, ${structureLine.type}"
						var position = (structureLine.coords as Vector2f).asWorldCoords(Math.ceil(structure.height / TILE_HEIGHT) as int)
						if (structure.width < TILE_WIDTH || structure.height < TILE_HEIGHT) {
							position.centerInCell(structure.width, structure.height)
						}
						structure.position = position

						// A special case for structures with secondary parts, namely the
						// weapons factory for its garage door
						var combineWith = structureData.shpFile.parts.body.combineWith
						if (combineWith) {
							structure.addBody(resourceManager.loadFile("${combineWith}.shp", ShpFile), getUnitData(combineWith))
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
							structure.addBib(bib)
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
