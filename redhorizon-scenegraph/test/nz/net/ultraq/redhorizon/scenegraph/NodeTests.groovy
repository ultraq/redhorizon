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

import spock.lang.Specification

/**
 * Tests for the {@link Node} class.
 *
 * @author Emanuel Rabina
 */
class NodeTests extends Specification {

	def 'find locates the matching descendant node'() {
		given:
			var root = new Node()
				.addChild(new Node().withName('Child')
					.addChild(new Node().withName('Grandchild')))
		when:
			var result = root.find { node -> node.name == 'Grandchild' }
		then:
			result.name == 'Grandchild'
	}
}
