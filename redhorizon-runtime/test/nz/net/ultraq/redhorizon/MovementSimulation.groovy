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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.engine.debug.DebugStore
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Rectangle
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.physics.BoxCollider
import nz.net.ultraq.redhorizon.physics.Collider
import nz.net.ultraq.redhorizon.physics.CollisionContinueEvent
import nz.net.ultraq.redhorizon.physics.CollisionStartEvent
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.objects.ScreenEdges
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.primitives.Rectanglef
import static org.lwjgl.glfw.GLFW.*

/**
 * A small simulation with a single moving object.
 *
 * @author Emanuel Rabina
 */
class MovementSimulation extends Application {

	private final Rectanglef sceneSize

	MovementSimulation(Rectanglef sceneSize) {

		super('Movement Simulation', 'test')
		this.sceneSize = sceneSize
	}

	@Override
	protected Scene configureScene(Scene scene) {

		scene.addChild(new ScreenEdges(sceneSize))
		scene.addChild(
			new Rectangle(10f, 10f, Colour.WHITE, true)
				.addChild(new MovementNode(100f))
				.addChild(new BoxCollider(10f, 10f))
				.addChild(new ScriptNode(MovementObjectScript))
		)
		scene.find(DebugOverlay).enable()
		scene.find(DebugStore).showMovementArrows = true
		return scene
	}

	/**
	 * Script to apply force to the movement object.
	 */
	static class MovementObjectScript extends Script<Rectangle> {

		private MovementNode movementNode

		@Override
		void init() {

			movementNode = node.find(MovementNode)

			var stopMovement = { Collider otherCollider ->
				if (otherCollider.parent instanceof ScreenEdges) {
					if (otherCollider.name == ScreenEdges.TOP_COLLIDER_NAME) {
						movementNode.vector.y = Math.min(movementNode.vector.y, 0f)
					}
					else if (otherCollider.name == ScreenEdges.BOTTOM_COLLIDER_NAME) {
						movementNode.vector.y = Math.max(movementNode.vector.y, 0f)
					}
					if (otherCollider.name == ScreenEdges.LEFT_COLLIDER_NAME) {
						movementNode.vector.x = Math.max(movementNode.vector.x, 0f)
					}
					else if (otherCollider.name == ScreenEdges.RIGHT_COLLIDER_NAME) {
						movementNode.vector.x = Math.min(movementNode.vector.x, 0f)
					}
				}
			}

			node.find(BoxCollider)
				.on(CollisionStartEvent) { event -> stopMovement(event.otherCollider()) }
				.on(CollisionContinueEvent) { event -> stopMovement(event.otherCollider()) }
		}

		@Override
		void update(float delta) {

			movementNode.vector.set(
				input.keyPressed(GLFW_KEY_A) || input.keyPressed(GLFW_KEY_LEFT) ? -1f :
					input.keyPressed(GLFW_KEY_D) || input.keyPressed(GLFW_KEY_RIGHT) ? 1f :
						0f,
				input.keyPressed(GLFW_KEY_W) || input.keyPressed(GLFW_KEY_UP) ? 1f :
					input.keyPressed(GLFW_KEY_S) || input.keyPressed(GLFW_KEY_DOWN) ? -1f :
						0f
			)
			if (movementNode.vector) {
				movementNode.vector.normalize()
			}
		}
	}
}
