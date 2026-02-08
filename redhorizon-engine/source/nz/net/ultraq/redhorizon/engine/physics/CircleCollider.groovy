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

package nz.net.ultraq.redhorizon.engine.physics

import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode

import org.joml.primitives.Circlef

/**
 * Give an entity a circular area which is used for detecting collisions with
 * other collision components.
 *
 * @author Emanuel Rabina
 */
class CircleCollider extends Collider<CircleCollider> {

	final float radius

	/**
	 * Constructor, set the collision area with a radius.
	 */
	CircleCollider(float radius) {

		this.radius = radius
	}

	@Override
	void checkCollision(Collider other) {

		// TODO: Allow collision checks across different shapes
		if (other !instanceof CircleCollider) {
			return
		}

		var position = globalPosition
		var scale = globalScale
		var bounds = new Circlef(position.x(), position.y(), radius * scale.x() as float)

		var otherPosition = other.globalPosition
		var otherScale = other.globalScale
		var otherBounds = new Circlef(otherPosition.x(), otherPosition.y(), other.radius * otherScale.x() as float)

		if (bounds.intersects(otherBounds)) {
			var scriptComponent = parent.findDescendentByType(ScriptNode)
			scriptComponent?.script?.onCollision(bounds, other.parent, otherBounds)

			var otherScriptComponent = other.parent.findDescendentByType(ScriptNode)
			otherScriptComponent?.script?.onCollision(otherBounds, parent, bounds)
		}
	}
}
