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

import nz.net.ultraq.redhorizon.CollisionSimulation.CollisionSimulationCandidateFunction
import nz.net.ultraq.redhorizon.runtime.Runtime

import org.joml.primitives.Rectanglef
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * Some performance tests are best done with the entire stack (instead of just
 * sending snapshot releases to games in development), so build them from
 * top-to-bottom here.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class PerformanceTests extends Specification {

	def 'Collision simulation'() {
		given:
			var simulation = new CollisionSimulation(new Rectanglef(-400f, -300f, 400f, 300f))
		when:
			new Runtime(simulation)
				.withSimulationMinimumUpdateFrequency(60)
				.withCollisionCandidatesFunction(new CollisionSimulationCandidateFunction())
				.execute()
		then:
			noExceptionThrown()
	}
}
