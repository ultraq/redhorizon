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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.graphics.GraphicsResource
import nz.net.ultraq.redhorizon.graphics.Texture

import org.joml.primitives.Rectanglef

import groovy.transform.TupleConstructor

/**
 * A sprite sheet or texture atlas is a texture made up of many smaller
 * textures, each representing some frame of a sprite or look of some asset,
 * with some way to locate each frame within it.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class SpriteSheet implements GraphicsResource {

	final Texture texture
	final int framesHorizontal
	final int framesVertical
	final float frameStepX
	final float frameStepY

	@Override
	void close() {

		texture.close()
	}

	/**
	 * Overload for the {@code []} operator, an alias for {@link #getFrame(int)}.
	 */
	Rectanglef getAt(int index) {

		return getFrame(index)
	}

	/**
	 * Return coordinates on the sprite sheet that would locate the sprite with
	 * the corresponding index from the raw data.
	 * <p>
	 * Note that with texture adjustments calculated in the shader, there
	 * shouldn't be a need for this any more.
	 */
	Rectanglef getFrame(int index) {

		var textureU = (index % framesHorizontal) * frameStepX as float
		var textureV = Math.floor(index / framesHorizontal) * frameStepY as float
		return new Rectanglef(
			textureU,
			textureV,
			textureU + frameStepX as float,
			textureV + frameStepY as float
		)
	}

	/**
	 * Return a texture region for use with this spritesheet.
	 */
	Rectanglef getTextureRegion() {

		return getFrame(0)
	}
}
