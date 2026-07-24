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

package nz.net.ultraq.redhorizon.scenegraph

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * Tests for the {@link Scene} class.
 *
 * @author Emanuel Rabina
 */
class SceneTests extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(SceneTests)

	static class MovementNode extends Node {}

	static class CollisionNode extends Node {}

	static class GraphicsNode extends Node {}

	static class ScriptNode extends Node {}

	def 'find performance'() {
		given:
			var scene = new Scene()
				.addChild(new MovementNode())
		when:
			var startTime = System.currentTimeMillis()
			while (true) {
				averageNanos('find', 1f, logger) { ->
					scene.find(MovementNode)
				}
				if (System.currentTimeMillis() - startTime > 5000) {
					break
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
	}

	def 'findAll performance'() {
		given:
			var scene = new Scene()
			100.times { i ->
				scene.addChild(
					new Node()
						.addChild(new MovementNode())
						.addChild(new CollisionNode())
						.addChild(new GraphicsNode())
						.addChild(new ScriptNode())
						.withName("Node ${i}")
				)
			}
		when:
			var startTime = System.currentTimeMillis()
			while (true) {
				average('findAll avg {}ms', 1f, logger) { ->
					scene.findAll(MovementNode)
					scene.findAll(CollisionNode)
					scene.findAll(GraphicsNode)
					scene.findAll(ScriptNode)
				}
				if (System.currentTimeMillis() - startTime > 5000) {
					break
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
	}
}
