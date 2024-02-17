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

import nz.net.ultraq.redhorizon.classic.nodes.FactionColours
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.units.Rotatable
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import java.nio.ByteBuffer

/**
 * A unit is a controllable object in the game.  As part of the Explorer
 * project, it is only stationary and used for showcasing the various
 * animations/states that it has.
 *
 * @author Emanuel Rabina
 */
class Unit extends Node<Unit> implements FactionColours, Rotatable {

	// TODO: Should this type of file be renamed to better reflect its purpose?
	final ImagesFile imagesFile
	final Palette palette
	final UnitData unitData

	private SpriteSheet spriteSheet
	private Texture paletteAsTexture

	Unit(ImagesFile imagesFile, Palette palette, UnitData unitData) {

		this.imagesFile = imagesFile
		this.palette = palette
		this.unitData = unitData
	}

	@Override
	void onSceneAdded(Scene scene) {

		// TODO: Load the palette once
		paletteAsTexture = scene
			.requestCreateOrGet(new TextureRequest(256, 1, palette.format, palette as ByteBuffer))
			.get()

		spriteSheet = scene
			.requestCreateOrGet(new SpriteSheetRequest(imagesFile.width, imagesFile.height, imagesFile.format, imagesFile.imagesData))
			.get()

		// TODO: Pick first frame for now, expecting to be able to change on render
		addChild(new PalettedSprite(imagesFile.width, imagesFile.height, spriteSheet, paletteAsTexture, spriteSheet.getFrame(0)))
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(spriteSheet, paletteAsTexture)
	}
}
