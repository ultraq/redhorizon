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

import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Rectangle
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.physics.BoxCollider
import nz.net.ultraq.redhorizon.physics.CollisionStartEvent
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.objects.ScreenEdges
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector2f
import org.joml.primitives.Rectanglef

/**
 * A small application with lots of collider entities.
 *
 * @author Emanuel Rabina
 */
class CollisionSimulation extends Application {

	private final Rectanglef sceneSize

	CollisionSimulation(Rectanglef sceneSize) {

		super('Collision Simulation', 'test')
		this.sceneSize = sceneSize
	}

	@Override
	protected Scene configureScene(Scene scene) {

		scene.addChild(new ScreenEdges(sceneSize))
		100.times { i ->
			scene.addChild(new Node()
				.addChild(new Rectangle(10f, 10f, new Colour("Random colour ${i}",
					Math.random() as float, Math.random() as float, Math.random() as float), true))
				.addChild(new BoxCollider(10f, 10f))
				.addChild(new MovementNode(Math.random() * 200f as float,
					new Vector2f(-1f + (Math.random() * 2f) as float, -1f + (Math.random() * 2) as float)))
				.addChild(new ScriptNode(BounceScript))
				.translate(
					sceneSize.minX + 5f + (Math.random() * (sceneSize.lengthX() - 10f)) as float,
					sceneSize.minY + 5f + (Math.random() * (sceneSize.lengthY() - 10f)) as float)
				.withName("Entity ${i}")
			)
		}
		scene.find(DebugOverlay).enable()
		return scene
	}

	static class BounceScript extends Script {

		@Override
		void init() {

			var movement = node.find(MovementNode)
			node.find(BoxCollider).on(CollisionStartEvent) { event ->
				var otherCollider = event.otherCollider()

				// Send the object back into the middle when hitting an edge
				if (otherCollider.parent instanceof ScreenEdges) {
					if (otherCollider.name == ScreenEdges.TOP_COLLIDER_NAME ||
						otherCollider.name == ScreenEdges.BOTTOM_COLLIDER_NAME) {
						movement.vector.y *= -1f
					}
					else if (otherCollider.name == ScreenEdges.LEFT_COLLIDER_NAME ||
						otherCollider.name == ScreenEdges.RIGHT_COLLIDER_NAME) {
						movement.vector.x *= -1f
					}
				}
			}
		}
	}
}
