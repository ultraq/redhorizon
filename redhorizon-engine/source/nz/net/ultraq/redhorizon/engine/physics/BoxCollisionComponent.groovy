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

import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent

import org.joml.primitives.Rectanglef

/**
 * Give an entity a 2D area which is used for detecting collisions with other
 * collision components.
 *
 * @author Emanuel Rabina
 */
class BoxCollisionComponent extends CollisionComponent<BoxCollisionComponent> {

	final float width
	final float height
	final Rectanglef bounds

	/**
	 * Constructor, set the collision area from width/height values.
	 */
	BoxCollisionComponent(float width, float height) {

		this.width = width
		this.height = height
		bounds = new Rectanglef(0, 0, width, height).center()
	}

	@Override
	void checkCollision(CollisionComponent other) {

		// TODO: Allow collision checks across different shapes
		if (other !instanceof BoxCollisionComponent) {
			return
		}

		var position = entity.globalPosition
		bounds.center().translate(position.x(), position.y())

		var otherPosition = other.entity.globalPosition
		var otherBounds = new Rectanglef(0, 0, other.width, other.height)
			.center()
			.translate(otherPosition.x(), otherPosition.y())

		if (bounds.intersectsRectangle(otherBounds)) {
			var scriptComponent = entity.findComponentByType(ScriptComponent) as ScriptComponent
			scriptComponent?.script?.onCollision(bounds, other.entity, otherBounds)

			var otherScriptComponent = other.entity.findComponentByType(ScriptComponent) as ScriptComponent
			otherScriptComponent?.script?.onCollision(otherBounds, entity, bounds)
		}
	}
}
