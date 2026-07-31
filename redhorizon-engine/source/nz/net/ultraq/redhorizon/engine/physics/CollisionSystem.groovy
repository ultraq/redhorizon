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

	private CollisionCandidatesFunction collisionCandidatesFunction = new DefaultCollisionCandidatesFunction()
	private final List<Collider> collisionCandidates = new ArrayList<>()
	private int lastCollisionCandidatesCount = 0
	private final List<CompletableFuture<Void>> collisionEvents = new ArrayList<>()
//	private int lastCollisionEventsCount = 0
	private final Map<Collider, Collider> collisions = new HashMap<>()

	@Override
	void update(Scene scene, float delta) {

		collisionEvents.clear()
		collisionCandidates.clear()

		collisionCandidatesFunction.calculate(scene, collisionCandidates)
		if (collisionCandidates.size() != lastCollisionCandidatesCount) {
			logger.debug('Collision candidates: {}', collisionCandidates.size())
			lastCollisionCandidatesCount = collisionCandidates.size()
		}

		for (var i = 0; i < collisionCandidates.size(); i += 2) {
			var collider = collisionCandidates[i]
			var otherCollider = collisionCandidates[i + 1]

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
		collisionEvents*.join()

//		if (collisionEvents.size() != lastCollisionEventsCount) {
//			if (collisionEvents) {
//				logger.debug('Collision events: {}', collisionEvents.size())
//			}
//			lastCollisionEventsCount = collisionEvents.size()
//		}
	}

	/**
	 * Update the collision check function used by the collision system.
	 */
	CollisionSystem withCollisionCandidatesFunction(CollisionCandidatesFunction collisionCandidatesFunction) {

		if (collisionCandidatesFunction) {
			this.collisionCandidatesFunction = collisionCandidatesFunction
		}
		return this
	}
}
