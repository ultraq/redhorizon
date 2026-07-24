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

package nz.net.ultraq.redhorizon.engine.debug

import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.graphics.Circle
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Rectangle
import nz.net.ultraq.redhorizon.physics.BoxCollider
import nz.net.ultraq.redhorizon.physics.CircleCollider
import nz.net.ultraq.redhorizon.physics.Collider
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Manage the drawing of collision outlines for debugging.
 *
 * @author Emanuel Rabina
 */
class DebugCollisionOutlineSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(DebugCollisionOutlineSystem)
	private static final String COLLISION_OUTLINE_NAME = 'Collision outline'

	private final List<Collider> colliders = new ArrayList<>()

	@Override
	void update(Scene scene, float delta) {

		average('Update: {}ms', 1f, logger) { ->
			var debugStore = scene.find(DebugStore)
			if (!debugStore) {
				throw new IllegalStateException('Scene does not have a DebugStore')
			}

			colliders.clear()
			scene.findAll(Collider, colliders).each { collider ->
				var collisionOutline = collider.parent.find(COLLISION_OUTLINE_NAME)
				if (debugStore.showCollisionOutlines) {
					if (!collisionOutline) {
						var collisionShape = switch (collider) {
							case BoxCollider -> new Rectangle(collider.width, collider.height, Colour.YELLOW)
							case CircleCollider -> new Circle(collider.radius, Colour.YELLOW)
							default -> null
						}
						if (collisionShape) {
							collisionOutline = collider.parent.addAndReturnChild(collisionShape.withName(COLLISION_OUTLINE_NAME))
						}
					}
					if (collider.enabled) {
						collisionOutline.enable()
					}
					else {
						collisionOutline.disable()
					}
				}
				else if (collisionOutline) {
					collisionOutline.disable()
				}
				return true
			}
		}
	}
}
