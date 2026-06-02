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
import nz.net.ultraq.redhorizon.engine.physics.CircleCollider
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Shape
import nz.net.ultraq.redhorizon.graphics.Vertex
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector3f
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

	@Override
	void update(Scene scene, float delta) {

		average('Update', 1f, logger) { ->
			var debugStore = scene.findByType(DebugStore)
			if (!debugStore) {
				throw new IllegalStateException('Scene does not have a DebugStore')
			}

			scene.traverse(CircleCollider) { CircleCollider collider ->
				var collisionOutline = collider.parent.findByName(COLLISION_OUTLINE_NAME)
				if (debugStore.showCollisionOutlines) {
					if (collider) {
						if (!collisionOutline) {
							var radius = collider.radius
							collisionOutline = collider.parent.addAndReturnChild(
								new Shape(Type.LINE_LOOP, new Vertex[]{
									new Vertex(new Vector3f(-radius as float, -radius as float, 0), Colour.YELLOW),
									new Vertex(new Vector3f(radius as float, -radius as float, 0), Colour.YELLOW),
									new Vertex(new Vector3f(radius as float, radius as float, 0), Colour.YELLOW),
									new Vertex(new Vector3f(-radius as float, radius as float, 0), Colour.YELLOW)
								})
									.withName(COLLISION_OUTLINE_NAME)
							)
						}
						if (collider.enabled) {
							collisionOutline.enable()
						}
						else {
							collisionOutline.disable()
						}
					}
				}
				else {
					if (collisionOutline) {
						collisionOutline.disable()
					}
				}
				return true
			}
		}
	}
}
