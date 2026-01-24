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

package nz.net.ultraq.redhorizon.engine.scripts

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.scenegraph.Scene

/**
 * Script-related extensions for the {@link Scene} class.
 *
 * @author Emanuel Rabina
 */
class SceneExtensions {

	/**
	 * Locate a script component by its type.
	 *
	 * TODO: This really helps the case that components should be traversable
	 */
	static <T extends EntityScript> T findScriptByType(Scene self, Class<T> scriptClass) {

		T result = null
		self.traverse(Entity) { Entity entity ->
			var scriptComponent = entity.findComponentByType(ScriptComponent)
			if (scriptComponent && scriptComponent.script.class == scriptClass) {
				result = scriptComponent.script as T
				return false
			}
			return true
		}
		return result
	}
}
