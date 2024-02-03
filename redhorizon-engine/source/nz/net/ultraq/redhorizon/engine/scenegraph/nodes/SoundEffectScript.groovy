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
 * A script for sound effects.  Can be used over the standard {@link Script}
 * class so that the sound effect has a better name within scripts.
 *
 * @author Emanuel Rabina
 */
abstract class SoundEffectScript extends Script<SoundEffect> {

	/**
	 * Adds an alias of {@code soundEffect} to the script target.
	 *
	 * @return
	 */
	protected SoundEffect getSoundEffect() {

		return scriptable
	}
}
