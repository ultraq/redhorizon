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
import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.scenegraph.Scene

import groovy.transform.TupleConstructor

/**
 * A system for updating any entity scripts.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ScriptSystem extends System {

	final ScriptEngine scriptEngine
	private final List<ScriptComponent> scriptComponents = new ArrayList<>()

	@Override
	void update(Scene scene, float delta) {

		scriptComponents.clear()
		scene.traverse(Entity) { Entity entity ->
			entity.findComponentsByType(ScriptComponent, scriptComponents)
		}
		scriptComponents.each { ScriptComponent component ->
			if (component.enabled) {
				component.update(scriptEngine, delta)
			}
		}
	}
}
