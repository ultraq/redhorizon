/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph.partitioning

import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.partioning.QuadTree

import org.joml.primitives.Rectanglef
import spock.lang.Specification

/**
 * Tests for our quadtree partioning solution.
 *
 * @author Emanuel Rabina
 */
class QuadTreeTests extends Specification {

	private final Rectanglef area = new Rectanglef(-10, -10, 10, 10)

	def "Adds a point"() {
		expect:
			var quadTree = new QuadTree(area)
			quadTree.add(new Node())
			quadTree.size() == 1
	}

	def "Subdivides when breaching capacity"() {
		expect:
			var quadTree = new QuadTree(area, 1)
			quadTree.add(new Node().tap {
				setPosition(3, 2)
			})
			quadTree.add(new Node().tap {
				setPosition(-5, 1)
			})
			quadTree.size() == 2
	}

	def "Can subdivide at the intersection of quadrants"() {
		expect:
			var quadTree = new QuadTree(area, 1)
			quadTree.add(new Node())
			quadTree.add(new Node())
			quadTree.size() == 2
	}
}
