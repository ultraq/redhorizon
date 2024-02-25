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

package nz.net.ultraq.redhorizon.classic.nodes

import nz.net.ultraq.redhorizon.classic.shaders.Shaders
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteMeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite

import org.joml.primitives.Rectanglef

/**
 * A sprite that requires a palette to fully realize its image.
 *
 * @author Emanuel Rabina
 */
class PalettedSprite extends Sprite implements FactionColours {

	final SpriteSheet spriteSheet
	final Texture palette

	/**
	 * Constructor, build this sprite from a region on a sprite sheet.
	 */
	PalettedSprite(int width, int height, SpriteSheet spriteSheet, Texture palette, Rectanglef region) {

		super(width, height, region)
		this.spriteSheet = spriteSheet
		this.palette = palette
	}

	@Override
	void onSceneAdded(Scene scene) {

		mesh = scene
			.requestCreateOrGet(new SpriteMeshRequest(bounds, region))
			.get()
		shader = scene
			.requestCreateOrGet(new ShaderRequest(Shaders.palettedSpriteShader))
			.get()
		material = new Material(
			texture: spriteSheet.texture,
			palette: palette,
			faction: faction
		)
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(mesh)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (material) {
			material.faction = faction
		}
		super.render(renderer)
	}
}
