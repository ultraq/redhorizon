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

import nz.net.ultraq.groovy.profilingextensions.LoggingStrategy
import nz.net.ultraq.groovy.profilingextensions.Profiler
import nz.net.ultraq.groovy.profilingextensions.TimedLoggingStrategy
import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor

/**
 * A system for running world simulation systems at various configurable rates.
 * Note that this isn't really a "physics" system as there are no physics
 * calculations involved, but rather the application of time to objects in a
 * scene which can be influenced by things like framerate.
 *
 * <p>By default, the simulation will run at the framerate.  This is mostly
 * desired but can lead to issues when there are massive spikes in frametimes,
 * like when debugging and inspecting a breakpoint, then returning to the game
 * to find everything has gone off the rails.  This can be mitigated by using
 * {@link #withMinimumUpdateFrequency}.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class SimulationSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(SimulationSystem)

	final CollisionSystem collisionSystem
	final MovementSystem movementSystem
	private float updateStep = Float.MAX_VALUE
	private float accumulatedTime = 0f
	private final LoggingStrategy loggingStrategy = new TimedLoggingStrategy(1f)
	private final List<Long> movementExecutionTimes = []
	private final List<Long> collisionExecutionTimes = []

	@Override
	void update(Scene scene, float delta) {

		// Perform either as many fixed-step updates within the accumulated frame,
		// time or run with the framerate.
		// Inspiration: http://gafferongames.com/game-physics/fix-your-timestep/
		accumulatedTime += delta
		var simulationDelta = Math.min(updateStep, delta)
		while (accumulatedTime > simulationDelta) {
			trackTime('SimulationSystem::collision') { ->
				collisionSystem.update(scene, simulationDelta)
			}
			trackTime('SimulationSystem::movement') { ->
				movementSystem.update(scene, simulationDelta)
			}
			accumulatedTime -= simulationDelta
		}

		if (loggingStrategy.shouldLog()) {
			logger.atDebug()
				.addMarker(Profiler.PROFILER_MARKER)
				.addMarker(Profiler.AVERAGE_MARKER)
				.setMessage('C: {}ms, M: {}ms')
				.addArgument(() -> sprintf('%.2f', getTimes('SimulationSystem::collision', collisionExecutionTimes).average()))
				.addArgument(() -> sprintf('%.2f', getTimes('SimulationSystem::movement', movementExecutionTimes).average()))
				.log()
		}
	}

	/**
	 * Configure the simulation to run at a minimum rate.
	 *
	 * @param minimumUpdateFrequency
	 *   The minimum rate at which physics updates should occur.  If the framerate
	 *   is below this value then simulation updates will still run at this rate.
	 *   If the framerate goes above then the simulation will match the framerate.
	 *   Use 0 to always let the simulation match the framerate.
	 */
	SimulationSystem withMinimumUpdateFrequency(int minimumUpdateFrequency) {

		updateStep = minimumUpdateFrequency ? 1 / minimumUpdateFrequency : Float.MAX_VALUE
		return this
	}
}
