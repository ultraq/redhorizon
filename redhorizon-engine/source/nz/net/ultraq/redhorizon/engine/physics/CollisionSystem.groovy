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
import nz.net.ultraq.redhorizon.physics.Collider
import nz.net.ultraq.redhorizon.physics.CollisionEndEvent
import nz.net.ultraq.redhorizon.physics.CollisionStartEvent
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CompletableFuture

/**
 * Perform collision checks between all entities in the scene.
 *
 * @author Emanuel Rabina
 */
class CollisionSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(CollisionSystem)

	private final List<Collider> colliders = new ArrayList<>()
	private int lastCollidersCount = 0
	private final List<CompletableFuture<Void>> collisionEvents = new ArrayList<>()
	private int lastCollisionEventsCount = 0
	private final Map<Collider, Collider> collisions = new HashMap<>()
	private int lastCollisionChecksCount = 0

	@Override
	void update(Scene scene, float delta) {

		average('Update: {}ms', 1f, logger) { ->
			colliders.clear()
			collisionEvents.clear()
			int collisionChecks = 0

			scene.findAll(Collider, colliders)
			if (colliders.size() != lastCollidersCount) {
				logger.debug('Colliders: {}', colliders.size())
				lastCollidersCount = colliders.size()
			}

			colliders.each { collider ->
				// Skip collision checks on disabled and stationary objects
				if (!collider.enabled || !collider.parent.find(MovementNode)) {
					return
				}
				colliders.each { otherCollider ->
					// Skip collision checks on disabled objects and itself
					if (!otherCollider.enabled || otherCollider == collider) {
						return
					}
					collisionChecks++

					var existingCollision = collisions[collider] == otherCollider
					if (collider.checkCollision(otherCollider)) {
						if (existingCollision) {
							// Do nothing - we don't have a 'collision continue' event
						}
						else {
							collisions[collider] = otherCollider
							collisions[otherCollider] = collider
							collisionEvents << collider.trigger(new CollisionStartEvent(otherCollider))
							collisionEvents << otherCollider.trigger(new CollisionStartEvent(collider))
						}
					}
					else if (existingCollision) {
						collisions.remove(collider)
						collisions.remove(otherCollider)
						collisionEvents << collider.trigger(new CollisionEndEvent(otherCollider))
						collisionEvents << otherCollider.trigger(new CollisionEndEvent(collider))
					}
				}
			}
			collisionEvents*.join()

			if (collisionEvents.size() != lastCollisionEventsCount) {
				if (collisionEvents) {
					logger.debug('Collision events: {}', collisionEvents.size())
				}
				lastCollisionEventsCount = collisionEvents.size()
			}

			if (collisionChecks != lastCollisionChecksCount) {
				logger.debug('Collision checks: {}', collisionChecks)
				lastCollisionChecksCount = collisionChecks
			}
		}
	}
}
