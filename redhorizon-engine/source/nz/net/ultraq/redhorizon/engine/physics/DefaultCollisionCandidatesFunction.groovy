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

import nz.net.ultraq.redhorizon.physics.Collider
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The default implementation of a collision candidates function, returns all
 * pairs of colliders where at least 1 member has a movement node.
 *
 * @author Emanuel Rabina
 */
class DefaultCollisionCandidatesFunction implements CollisionCandidatesFunction {

	private static final Logger logger = LoggerFactory.getLogger(DefaultCollisionCandidatesFunction)

	private final List<Collider> colliders = new ArrayList<>()
	private int lastCollidersCount = 0

	@Override
	List<Collider> calculate(Scene scene, List<Collider> results) {

		colliders.clear()
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
				results << collider
				results << otherCollider
			}
		}

		return results
	}
}
