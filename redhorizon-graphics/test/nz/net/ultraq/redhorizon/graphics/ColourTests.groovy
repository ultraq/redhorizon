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

package nz.net.ultraq.redhorizon.graphics

import spock.lang.Specification

/**
 * Tests for the {@link Colour} class.
 *
 * @author Emanuel Rabina
 */
class ColourTests extends Specification {

	def 'Create a colour from a hex code'() {
		expect:
			Colour.fromHexCode('#ff0000', 'Red') == Colour.RED
	}

	def 'Create a colour from a hex code (no name specified)'() {
		expect:
			var colour = Colour.fromHexCode('#ff0000')
			colour.r == 1f
			colour.g == 0f
			colour.b == 0f
			colour.a == 1f
			colour.name == '#ff0000'
	}
}
