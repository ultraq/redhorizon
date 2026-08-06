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

import org.joml.primitives.Rectanglef
import spock.lang.Specification

/**
 * Tests for the {@link Rectanglefc} class.
 *
 * @author Emanuel Rabina
 */
class RectanglefcTests extends Specification {

	def 'Create a read-only rectangle'() {
		given:
			var rect = new Rectanglef(5, 5, 8, 8)
		when:
			var readOnlyRect = new Rectanglefc(rect)
		then:
			readOnlyRect.minX == rect.minX
			readOnlyRect.minY == rect.minY
			readOnlyRect.maxX == rect.maxX
			readOnlyRect.maxY == rect.maxY
	}
}
