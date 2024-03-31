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
import nz.net.ultraq.redhorizon.classic.maps.TileSet
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

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

	private Texture paletteAsTexture

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

		tileSet = new TileSet(palette)

		addChild(new BackgroundLayer())
		addChild(new MapLines(this))
	}

	@Override
	void onSceneAdded(Scene scene) {

		// TODO: Load the palette once
		paletteAsTexture = scene
			.requestCreateOrGet(new TextureRequest(256, 1, palette.format, palette as ByteBuffer))
			.get()
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
	 * Special layer for the background image.
	 */
	private class BackgroundLayer extends Node<BackgroundLayer> {

		private SpriteSheet background

		@Override
		void onSceneAdded(Scene scene) {

			var clearTileName = MapRAMapPackTile.DEFAULT.name + theater.ext
			var tileFile = resourceManager.loadFile(clearTileName, TmpFileRA)

			// Create a sprite sheet for the 5x4 background tile
			var imageData = tileFile.imagesData.combineImages(tileFile.width, tileFile.height, tileFile.format, theater.clearX)
			var spriteWidth = tileFile.width * theater.clearX
			var spriteHeight = tileFile.height * theater.clearY
			background = scene
				.requestCreateOrGet(new SpriteSheetRequest(spriteWidth, spriteHeight, tileFile.format, imageData))
				.get()

			// Use the sprite sheet and repeat it over the entire map area
			var backgroundWidth = TILES_X * TILE_WIDTH
			var backgroundHeight = TILES_Y * TILE_HEIGHT
			var repeatX = backgroundWidth / spriteWidth as float
			var repeatY = backgroundHeight / spriteHeight as float
			addChild(new PalettedSprite(backgroundWidth, backgroundHeight, tileFile.numImages, background, repeatX, repeatY, palette))
		}

		@Override
		void onSceneRemoved(Scene scene) {

			scene.requestDelete(background)
		}
	}
}
