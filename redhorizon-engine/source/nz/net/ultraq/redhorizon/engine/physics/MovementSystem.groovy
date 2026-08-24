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

package nz.net.ultraq.redhorizon.engine.physics

import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector2f

/**
 * Perform movement of all movable objects in a scene.
 *
 * @author Emanuel Rabina
 */
class MovementSystem extends System {

	private final List<MovementNode> movementNodes = new ArrayList<>()
	private final Vector2f target = new Vector2f()

	@Override
	void update(Scene scene, float delta) {

		movementNodes.clear()
		scene.findAll(MovementNode, movementNodes).each { node ->
			if (node.enabled) {
				if (node.vector) {
					node.speed = node.maxSpeed
					target.set(node.vector).mul(node.speed * delta as float)
					node.parent.translate(target.x, target.y)
					node.velocity.set(node.vector).mul(node.speed)
				}
				else {
					node.speed = 0f
					node.velocity.set(0f, 0f)
				}
			}
		}
	}
}
