/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.graphics

import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader.PalettedSpriteShaderContext
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsComponent
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Shader

import groovy.transform.TupleConstructor

/**
 * A component for adding a palette to an entity.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false, includes = ['palette'])
class PaletteComponent implements GraphicsComponent<PaletteComponent, PalettedSpriteShaderContext>, AutoCloseable {

	Palette palette
	final Class<? extends Shader> shaderClass = PalettedSpriteShader

	@Override
	void render(PalettedSpriteShaderContext shaderContext) {

		shaderContext.setPalette(palette)
	}

	@Override
	void close() {

		palette.close()
	}
}
