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

import org.joml.primitives.Rectanglef

import groovy.transform.TupleConstructor

/**
 * Give an entity a 2D area which is used for detecting collisions with other
 * collision components.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class BoxCollider extends Collider<BoxCollider, Rectanglef> {

	final float width
	final float height
	private final Rectanglef _bounds = new Rectanglef(0, 0, width, height)

	@Override
	boolean checkCollision(Collider other) {

		// TODO: Allow collision checks across different shapes
		if (other !instanceof BoxCollider) {
			return false
		}

		return bounds.intersectsRectangle(other.bounds)
	}

	@Override
	Rectanglef getBounds() {

		var position = globalPosition
		return _bounds.center().translate(position.x(), position.y())
	}
}
