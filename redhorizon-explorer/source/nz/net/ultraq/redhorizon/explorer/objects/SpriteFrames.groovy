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

import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.nodes.FactionColours
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.Palette

import groovy.transform.TupleConstructor
import java.nio.ByteBuffer

/**
 * A simple, frame-at-a-time viewer or a sprite file.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false, includes = ['shpFile', 'palette'])
class SpriteFrames extends Node<SpriteFrames> implements FactionColours {

	final ShpFile shpFile
	final Palette palette

	SpriteSheet spriteSheet
	int currentFrame
	PalettedSprite palettedSprite

	private Texture paletteAsTexture

	@Override
	void onSceneAdded(Scene scene) {

		var width = shpFile.width
		var height = shpFile.height

		// TODO: Load the palette once
		paletteAsTexture = scene
			.requestCreateOrGet(new TextureRequest(256, 1, palette.format, palette as ByteBuffer))
			.get()

		spriteSheet = scene
			.requestCreateOrGet(new SpriteSheetRequest(width, height, shpFile.format, shpFile.imagesData))
			.get()

		palettedSprite = new PalettedSprite(width, height, spriteSheet, paletteAsTexture, spriteSheet[currentFrame])
		addChild(palettedSprite)
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(paletteAsTexture, spriteSheet)
	}
}
