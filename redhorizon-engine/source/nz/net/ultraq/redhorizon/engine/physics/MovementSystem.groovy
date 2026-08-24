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
					node.lastVector.lerp(node.vector, 1f * delta as float, target)
					node.parent.translate(target.x, target.y)
					node.lastVector.set(target)
//					node.speed = Math.min(node.speed + (node.maxSpeed * node.acceleration * delta) as float, node.maxSpeed)
//					target.set(node.vector).mul(node.speed * delta as float)
//					node.parent.translate(target.x, target.y)
//					node.velocity.set(node.vector).mul(node.speed)
//					node.lastVector.set(node.vector)
				}
				else if (node.lastVector) {
					node.lastVector.lerp(node.vector, 1f * delta as float, target)
					node.parent.translate(target.x, target.y)
					node.lastVector.set(target)
//					node.speed = Math.max(node.speed - (node.maxSpeed * node.acceleration * delta) as float, 0f)
//					target.set(node.lastVector).mul(node.speed * delta as float)
//					node.parent.translate(target.x, target.y)
//					node.velocity.set(node.lastVector).mul(node.speed)
				}
			}
		}
	}
}
