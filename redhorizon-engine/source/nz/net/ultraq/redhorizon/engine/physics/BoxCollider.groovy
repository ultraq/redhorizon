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

import nz.net.ultraq.eventhorizon.EventTarget

import org.joml.primitives.Rectanglef

/**
 * Give an entity a 2D area which is used for detecting collisions with other
 * collision components.
 *
 * @author Emanuel Rabina
 */
class BoxCollider extends Collider<BoxCollider> implements EventTarget<BoxCollider> {

	final float width
	final float height
	final Rectanglef bounds

	/**
	 * Constructor, set the collision area from width/height values.
	 */
	BoxCollider(float width, float height) {

		this.width = width
		this.height = height
		bounds = new Rectanglef(0, 0, width, height).center()
	}

	@Override
	void checkCollision(Collider other) {

		// TODO: Allow collision checks across different shapes
		if (other !instanceof BoxCollider) {
			return
		}

		var position = globalPosition
		bounds.center().translate(position.x(), position.y())

		var otherPosition = other.globalPosition
		var otherBounds = new Rectanglef(0, 0, other.width, other.height)
			.center()
			.translate(otherPosition.x(), otherPosition.y())

		if (bounds.intersectsRectangle(otherBounds)) {
			trigger(new CollisionEvent(bounds, other.parent, otherBounds))
			other.trigger(new CollisionEvent(otherBounds, parent, bounds))
		}
	}
}
