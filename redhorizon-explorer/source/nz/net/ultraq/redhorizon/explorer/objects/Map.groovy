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
import nz.net.ultraq.redhorizon.classic.maps.Theater
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
	final String name
	final Theater theater
	final Rectanglef boundary
	final Vector2f initialPosition

	private final Palette palette

	/**
	 * Constructor, create a new map from a map file.
	 */
	Map(MapFile mapFile, ResourceManager resourceManager) {

		this.mapFile = mapFile

		name = mapFile.name

		var mapSection = mapFile.mapSection
		theater = Theater.valueOf(mapSection.theater())
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

		addChild(new MapLines(this))
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
}
