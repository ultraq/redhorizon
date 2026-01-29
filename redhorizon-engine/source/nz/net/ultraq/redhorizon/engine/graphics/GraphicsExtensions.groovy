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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.SpriteSheet

/**
 * Graphics-related extensions.
 *
 * @author Emanuel Rabina
 */
class GraphicsExtensions {

	/**
	 * Convenience method to add a sprite component to an entity.
	 */
	static Entity addSprite(Entity self, Image image, Class<? extends Shader> shaderClass) {

		return self.addComponent(new SpriteComponent(image, shaderClass))
	}

	/**
	 * Convenience method to add a sprite component to an entity.
	 */
	static Entity addSprite(Entity self, SpriteSheet spriteSheet, Class<? extends Shader> shaderClass) {

		return self.addComponent(new SpriteComponent(spriteSheet, shaderClass))
	}
}
