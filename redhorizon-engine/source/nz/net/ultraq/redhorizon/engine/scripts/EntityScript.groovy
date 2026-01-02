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

package nz.net.ultraq.redhorizon.engine.scripts

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.input.InputEventHandler

/**
 * A custom class for any script used during the game update phase.
 *
 * @author Emanuel Rabina
 */
abstract class EntityScript<T extends Entity> {

	protected T entity
	protected InputEventHandler input

	/**
	 * Called when a script is first started, can be overridden to set up the
	 * script.
	 */
	void init() {
	}

	/**
	 * Called when a collision occurs between the entity this script is attached
	 * to and another entity with a collision object.
	 *
	 * @param thisBounds
	 *   Bounds of the collision object on this entity.
	 * @param otherEntity
	 *   The other entity that the collision object belongs to.
	 * @param otherBounds
	 *   Bounds of the collision object on the other entity.
	 */
	void onCollision(Object thisBounds, Entity otherEntity, Object otherBounds) {
	}

	/**
	 * Actions to perform when the entity is being updated.
	 */
	void update(float delta) {
	}
}
