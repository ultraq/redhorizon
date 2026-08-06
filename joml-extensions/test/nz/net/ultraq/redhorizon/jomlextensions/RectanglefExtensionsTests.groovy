/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.jomlextensions

import org.joml.Vector2f
import org.joml.primitives.Circlef
import org.joml.primitives.Rectanglef
import spock.lang.Specification

/**
 * Tests for the {@link RectanglefExtensions} methods.
 *
 * @author Emanuel Rabina
 */
class RectanglefExtensionsTests extends Specification {

	def 'asType - Returns a read-only rectangle'() {
		given:
			var rect = new Rectanglef(5, 5, 8, 8)
		when:
			var readOnlyRect = rect as Rectanglefc
		then:
			readOnlyRect.minX == rect.minX
			readOnlyRect.minY == rect.minY
			readOnlyRect.maxX == rect.maxX
			readOnlyRect.maxY == rect.maxY
	}

	def '#center - Center a rectangle'() {
		given:
			var rect = new Rectanglef(5, 5, 8, 8)
		when:
			rect.center()
		then:
			rect.minX == -1.5f
			rect.minY == -1.5f
			rect.maxX == 1.5f
			rect.maxY == 1.5f
	}

	def '#getMax - Retrieve the maxX/maxY components'() {
		given:
			var rect = new Rectanglef(1, 2, 3, 4)
		when:
			var result = new Vector2f()
			rect.getMax(result)
		then:
			result.x == 3
			result.y == 4
	}

	def '#getMin - Retrieve the minX/minY components'() {
		given:
			var rect = new Rectanglef(1, 2, 3, 4)
		when:
			var result = new Vector2f()
			rect.getMin(result)
		then:
			result.x == 1
			result.y == 2
	}

	def '#intersectsCircle - Returns true when a rectangle intersects a circle'() {
		given:
			var rect = new Rectanglef(0f, 0f, 3f, 4f)
		expect:
			rect.intersectsCircle(new Circlef(4f, 0f, 2f))
	}

	def '#intersectsCircle - Returns true when a circle\'s center is inside a rectangle'() {
		given:
			var rect = new Rectanglef(0f, 0f, 4f, 4f)
		expect:
			rect.intersectsCircle(new Circlef(2f, 2f, 1f))
	}

	def '#set - Set all values in one method call'() {
		given:
			var rect = new Rectanglef()
		when:
			rect.set(1, 2, 3, 4)
		then:
			rect.minX == 1
			rect.minY == 2
			rect.maxX == 3
			rect.maxY == 4
	}

	def '#setLengths - Set lengths'() {
		given:
			var rect = new Rectanglef(2, 2, 4, 4)
		when:
			rect.setLengths(3, 3)
		then:
			rect.minX == 2
			rect.minY == 2
			rect.maxX == 5
			rect.maxY == 5
	}
}
