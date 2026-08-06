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
import spock.lang.Specification

/**
 * Tests for the {@link Vector2fExtensions} methods.
 *
 * @author Emanuel Rabina
 */
class Vector2fExtensionsTests extends Specification {

	def 'A Vector2f is considered falsey if null or has no length'(Vector2f vector) {
		expect:
			!vector
		where:
			vector << [null, new Vector2f(), new Vector2f(0, 0)]
	}

	def 'A Vector2f is considered truthy if non-null and has length'(Vector2f vector) {
		expect:
			vector
		where:
			vector << [new Vector2f(1, 2)]
	}
}
