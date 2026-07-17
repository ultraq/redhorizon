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

import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * Tests for the {@link CollisionSystem}.
 *
 * @author Emanuel Rabina
 */
class CollisionSystemTests extends Specification {

	def 'Triggers a collision start event when 2 colliders intersect'() {
		given:
			var scene = new Scene()
				.addChild(new Node().addChild(new BoxCollider(10f, 10f)))
				.addChild(new Node().addChild(new BoxCollider(10f, 10f)))
			var collisionSystem = new CollisionSystem()
			var hasCollisionStartEvent = false
			scene.find(BoxCollider).on(CollisionStartEvent) { event ->
				hasCollisionStartEvent = true
			}
		when:
			collisionSystem.update(scene, 0f)
		then:
			new PollingConditions().eventually { ->
				assert hasCollisionStartEvent
			}
	}
}
