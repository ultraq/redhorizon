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

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Perform collision checks between all entities in the scene.
 *
 * @author Emanuel Rabina
 */
class CollisionSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(CollisionSystem)

	private final List<CollisionComponent> collisionComponents = new ArrayList<>()

	@Override
	void update(Scene scene, float delta) {

		average('Collision detection', 1f, logger) { ->
			collisionComponents.clear()
			scene.traverse(Entity) { Entity entity ->
				entity.findComponentsByType(CollisionComponent, collisionComponents)
			}
			for (var i = 0; i < collisionComponents.size(); i++) {
				var collision = collisionComponents.get(i)
				if (!collision.enabled) {
					continue
				}
				for (var j = i + 1; j < collisionComponents.size(); j++) {
					var otherCollision = collisionComponents.get(j)
					if (!otherCollision.enabled) {
						continue
					}
					collision.checkCollision(otherCollision)
				}
			}
		}
	}
}
