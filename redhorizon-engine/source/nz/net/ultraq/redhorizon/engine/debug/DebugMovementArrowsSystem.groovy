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
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.scenegraph.Scene

/**
 * Manage the drawing of movement vectors for debugging.
 *
 * @author Emanuel Rabina
 */
class DebugMovementArrowsSystem extends System {

	private static final String MOVEMENT_ARROW_NAME = 'Movement arrow'
	private static final String VECTOR_ARROW_NAME = 'Vector arrow'

	private final List<MovementNode> movementNodes = new ArrayList<>()

	@Override
	void update(Scene scene, float delta) {

		var debugStore = scene.find(DebugStore)
		if (!debugStore) {
			throw new IllegalStateException('Scene does not have a DebugStore')
		}

		movementNodes.clear()
		scene.findAll(MovementNode, movementNodes).each { node ->
			var movementArrow = node.find(MOVEMENT_ARROW_NAME)
			var vectorArrow = node.find(VECTOR_ARROW_NAME)
			if (debugStore.showMovementArrows) {
				if (!movementArrow && !vectorArrow) {
					movementArrow = node.addAndReturnChild(new MovementLine(node.vector, node.maxSpeed * 0.25f as float, Colour.YELLOW)
						.withName(MOVEMENT_ARROW_NAME))
					vectorArrow = node.addAndReturnChild(new MovementLine(node.lastVector, node.maxSpeed * 0.25f as float, Colour.GREEN)
						.withName(VECTOR_ARROW_NAME))
				}
				if (node.enabled) {
					movementArrow.enable()
					vectorArrow.enable()
				}
				else {
					movementArrow.disable()
					vectorArrow.disable()
				}
			}
			else if (movementArrow && vectorArrow) {
				movementArrow.disable()
				vectorArrow.disable()
			}
		}
	}
}
