/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.classic.filetypes.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.TmpFileRADecoder
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.resources.ResourceManager
import nz.net.ultraq.redhorizon.scenegraph.Node

import org.joml.primitives.Rectanglef

import groovy.transform.TupleConstructor

/**
 * A map loader for Red Alert maps.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class RedAlertMapLoader implements MapLoader<IniFile> {

	final ResourceManager resourceManager

	@Override
	Node load(IniFile iniFile) {

		var mapSection = iniFile['Map']
		var theater = Theater.valueOf(mapSection['Theater'])

		var boundary = new Rectanglef()
			.setMin(mapSection['X'] as int, mapSection['Y'] as int)
			.setLengths(mapSection['Width'] as int, mapSection['Height'] as int)

		return new Node()
			.withName('Map')
			.addChild(new MapBackground(theater, boundary))
	}

	/**
	 * The repeating background layer of a map.
	 */
	private class MapBackground extends Node<MapBackground> {

		MapBackground(Theater theater, Rectanglef boundary) {

			var clearTileName = "${MapRAMapPackTile.DEFAULT.name}.${theater.ext}"
			var clearSpriteSheet = resourceManager.loadFile(clearTileName).withCloseable { inputStream ->
				return new SpriteSheet(clearTileName, new TmpFileRADecoder(), inputStream, theater.clearX)
			}
			var clearSprite = resourceManager.manageResource(
				new Sprite(128 * 24, 128 * 24, 128 / theater.clearX as float, 128 / theater.clearY as float,
					clearSpriteSheet.texture, PalettedSpriteShader, clearSpriteSheet))
			addChild(clearSprite.translate(boundary.minX, boundary.minY))
		}
	}
}
