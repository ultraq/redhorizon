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

package nz.net.ultraq.redhorizon.explorer.objects

import nz.net.ultraq.redhorizon.classic.filetypes.MapFile
import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileRA
import nz.net.ultraq.redhorizon.classic.maps.MapRAMapPackTile
import nz.net.ultraq.redhorizon.classic.maps.Theater
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.shaders.Shaders
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
	final String name = "Map - ${mapFile.name}"
	final Theater theater
	final Rectanglef boundary
	final Vector2f initialPosition

	private final Palette palette
	private final TileSet tileSet
	private final ResourceManager resourceManager

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
			return new PalFile(inputStream).withAlphaMask()
		}

		var mapXY = new Vector2f(mapSection.x(), mapSection.y())
		var mapWH = new Vector2f(mapXY).add(mapSection.width(), mapSection.height())
		boundary = new Rectanglef(mapXY.asWorldCoords(), mapWH.asWorldCoords()).makeValid()

		var waypoints = mapFile.waypointsData
		var waypoint98 = waypoints[98]
		initialPosition = waypoint98.asCellCoords().asWorldCoords()

		var halfMapWidth = (TILES_X * TILE_WIDTH) / 2 as float
		var halfMapHeight = (TILES_Y * TILE_HEIGHT) / 2 as float
		bounds.set(-halfMapWidth, -halfMapHeight, halfMapWidth, halfMapHeight)

		tileSet = new TileSet()

		addChild(new MapBackground())
		addChild(new MapPack())
		addChild(new MapLines())
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		tileSetSpriteSheetFuture = CompletableFuture.supplyAsync { ->
			return tileSet.tileFileList
				.collect { tileFile -> tileFile.imagesData }
				.flatten()
				.toArray(new ByteBuffer[0])
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
			 - Name: ${mapFile.name}
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

		private static final Vector2f X_AXIS_MIN = new Vector2f(-3600, 0)
		private static final Vector2f X_AXIS_MAX = new Vector2f(3600, 0)
		private static final Vector2f Y_AXIS_MIN = new Vector2f(0, -3600)
		private static final Vector2f Y_AXIS_MAX = new Vector2f(0, 3600)

		MapLines() {

			addChild(new Primitive(MeshType.LINES, Colour.RED.withAlpha(0.5), X_AXIS_MIN, X_AXIS_MAX, Y_AXIS_MIN, Y_AXIS_MAX).tap {
				name = "XY axis (red)"
			})
			addChild(new Primitive(MeshType.LINE_LOOP, Colour.YELLOW.withAlpha(0.5), boundary as Vector2f[]).tap {
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
			var backgroundWidth = TILES_X * TILE_WIDTH
			var backgroundHeight = TILES_Y * TILE_HEIGHT
			var repeatX = backgroundWidth / spriteWidth as float
			var repeatY = backgroundHeight / spriteHeight as float

			var backgroundSprite = new PalettedSprite(backgroundWidth, backgroundHeight, tileFile.numImages, repeatX, repeatY, palette, { scene ->
				return scene.requestCreateOrGet(new SpriteSheetRequest(spriteWidth, spriteHeight, tileFile.format, imageData))
			})
			backgroundSprite.bounds.center()
			addChild(backgroundSprite)
		}
	}

	/**
	 * Representation of a single tile on the map, used for combining with other
	 * tiles to create a single large mesh for rendering a whole layer in a single
	 * draw call.
	 */
	private static record MapTile(Vector2f position, int frameInTileSet) {}

	/**
	 * The "MapPack" layer of a Red Alert map.
	 */
	private class MapPack extends Node<MapPack> implements GraphicsElement {

		private final List<MapTile> mapTiles = []
		private Mesh fullMesh
		private Shader shader
		private Material material

		MapPack() {

			var tileData = mapFile.mapPackData

			TILES_X.times { y ->
				TILES_Y.times { x ->

					// Get the byte representing the tile
					var tileValOffset = y * TILES_Y + x
					var tileVal = tileData.getShort(tileValOffset * 2)
					var tilePic = tileData.get(TILES_X * TILES_Y * 2 + tileValOffset) & 0xff

					// Retrieve the appropriate tile, skip empty and default tiles
					if (tileVal != 0xff && tileVal != -1) {
						var tile = MapRAMapPackTile.values().find { tile -> tile.value == tileVal }

						// Some unknown tile types still coming through?
						if (!tile) {
							logger.warn("Skipping unknown mappack tile type: ${tileVal}")
							return
						}
						var tileFile = resourceManager.loadFile(tile.name + theater.ext, TmpFileRA)

						// Skip references to invalid tiles
						if (tilePic >= tileFile.imagesData.length) {
							return
						}

						tileSet.addTiles(tileFile)
						var mapTile = new MapTile(new Vector2f(x, y).asWorldCoords(1), tileSet.getFrame(tileFile, tilePic))
						mapTiles << mapTile

						bounds.setMin(
							Math.min(bounds.minX, mapTile.position().x),
							Math.min(bounds.minY, mapTile.position().y)
						)
						bounds.setMax(
							Math.max(bounds.maxX, mapTile.position().x + TILE_WIDTH) as float,
							Math.max(bounds.maxY, mapTile.position().y + TILE_HEIGHT as float)
						)
					}
				}
			}
		}

		@Override
		String getName() {

			return "MapPack - ${mapTiles.size()} tiles"
		}

		@Override
		CompletableFuture<Void> onSceneAdded(Scene scene) {

			material = new Material()

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
							.thenApplyAsync { newMesh ->
								fullMesh = newMesh
								return newMesh
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
				}
			)
		}

		@Override
		CompletableFuture<Void> onSceneRemoved(Scene scene) {

			return scene.requestDelete(fullMesh)
		}

		@Override
		void render(GraphicsRenderer renderer) {

			if (fullMesh && shader && material) {
				renderer.draw(fullMesh, globalTransform, shader, material)
			}
		}
	}
}
