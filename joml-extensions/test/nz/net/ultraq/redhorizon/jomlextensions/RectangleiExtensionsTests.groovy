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

import org.joml.primitives.Rectanglei
import spock.lang.Specification

/**
 * Tests for the {@link RectangleiExtensions} methods.
 *
 * @author Emanuel Rabina
 */
class RectangleiExtensionsTests extends Specification {

	def 'Set all values in one method call'() {
		given:
			Rectanglei rectangle = new Rectanglei()
		when:
			rectangle.set(1, 2, 3, 4)
		then:
			rectangle.minX == 1
			rectangle.minY == 2
			rectangle.maxX == 3
			rectangle.maxY == 4
	}

	def 'Set lengths'() {
		given:
			Rectanglei rectangle = new Rectanglei(2, 2, 4, 4)
		when:
			rectangle.setLengths(3, 3)
		then:
			rectangle.minX == 2
			rectangle.minY == 2
			rectangle.maxX == 5
			rectangle.maxY == 5
	}
}
