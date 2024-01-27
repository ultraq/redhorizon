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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.engine.scripting.Script

/**
 * A script for sprites.  Can be used over the standard {@link Script} class so
 * that the sprite has a better name within scripts.
 *
 * @author Emanuel Rabina
 */
abstract class SpriteScript extends Script<Sprite> {

	/**
	 * Adds an alias of {@code sprite} to the script target.
	 */
	protected Sprite getSprite() {

		return scriptable
	}
}
