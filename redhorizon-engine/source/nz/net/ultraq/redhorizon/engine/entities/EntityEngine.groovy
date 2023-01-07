/* 
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.entities

import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.scenegraph.ElementAddedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.ElementRemovedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import com.badlogic.ashley.core.Entity

/**
 * The engine that drives updates for the entity component system.
 * <p>
 * Currently wraps libgdx's Ashley ECS under the hood, bridging it's API and
 * what we have so far in Red Horizon.
 *
 * @author Emanuel Rabina
 */
class EntityEngine extends Engine {

	private final com.badlogic.ashley.core.Engine ashleyEngine

	/**
	 * Constructor, tie the entity engine to a scene so that it can be notified of
	 * entities being attached to it.
	 *
	 * @param scene
	 */
	EntityEngine(Scene scene) {

		ashleyEngine = new com.badlogic.ashley.core.Engine()

		scene.on(ElementAddedEvent) { event ->
			var element = event.element
			if (element instanceof Entity) {
				ashleyEngine.addEntity(element)
			}
		}
		scene.on(ElementRemovedEvent) { event ->
			var element = event.element
			if (element instanceof Entity) {
				ashleyEngine.removeEntity(element)
			}
		}
	}
}
