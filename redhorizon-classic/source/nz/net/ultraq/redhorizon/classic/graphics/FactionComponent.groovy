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

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader.PalettedSpriteShaderContext
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsComponent
import nz.net.ultraq.redhorizon.graphics.Shader

/**
 * A palette component for adjusting the colours of a sprite.
 *
 * @author Emanuel Rabina
 */
class FactionComponent implements GraphicsComponent<FactionComponent, PalettedSpriteShaderContext>, AutoCloseable {

	final Class<? extends Shader> shaderClass = PalettedSpriteShader

	@Delegate(interfaces = false, includes = ['getFaction', 'setFaction'])
	final FactionAdjustmentMap adjustmentMap

	/**
	 * Constructor, sets which faction this component is for.
	 */
	FactionComponent(Faction faction) {

		adjustmentMap = new FactionAdjustmentMap(faction)
	}

	@Override
	void close() {

		adjustmentMap.close()
	}

	@Override
	void render(PalettedSpriteShaderContext shaderContext) {

		shaderContext.setAdjustmentMap(adjustmentMap)
		adjustmentMap.update()
	}
}
