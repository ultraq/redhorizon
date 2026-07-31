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

/**
 * A system for running world simulation systems at a fixed rate, decoupled from
 * frame rate, to prevent jank.
 *
 * @author Emanuel Rabina
 */
class PhysicsSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(PhysicsSystem)

	private final MovementSystem movementSystem
	private final CollisionSystem collisionSystem
	private final float updateStep
	private float accumulatedTime = 0f
	private final LoggingStrategy loggingStrategy = new TimedLoggingStrategy(1f)
	private final List<Long> movementExecutionTimes = []
	private final List<Long> collisionExecutionTimes = []

	/**
	 * Constructor, configure the physics system.
	 *
	 * @param updateFrequency
	 *   The rate at which physics updates should occur.  Use 0 to let the physics
	 *   updates run at the same rate as the frame rate.
	 * @param movementSystem
	 * @param collisionSystem
	 */
	PhysicsSystem(int updateFrequency, MovementSystem movementSystem, CollisionSystem collisionSystem) {

		updateStep = updateFrequency ? 1 / updateFrequency : 0f
		this.movementSystem = movementSystem
		this.collisionSystem = collisionSystem
	}

	@Override
	void update(Scene scene, float delta) {

		// Perform as many fixed-step updates within the accumulated frame time
		// From: http://gafferongames.com/game-physics/fix-your-timestep/
		if (updateStep) {
			accumulatedTime += delta
			while (accumulatedTime > updateStep) {
				updateSystems(scene, updateStep)
				accumulatedTime -= updateStep
			}
		}
		// Run updates at the speed of the framerate
		else {
			updateSystems(scene, delta)
		}

		if (loggingStrategy.shouldLog()) {
			logger.atDebug()
				.addMarker(Profiler.PROFILER_MARKER)
				.addMarker(Profiler.AVERAGE_MARKER)
				.setMessage('M: {}ms, C: {}ms')
				.addArgument(() -> sprintf('%.2f', getTimes('PhysicsSystem::movement', movementExecutionTimes).average()))
				.addArgument(() -> sprintf('%.2f', getTimes('PhysicsSystem::collision', collisionExecutionTimes).average()))
				.log()
		}
	}

	/**
	 * Run each of the physics subsystems.
	 */
	private void updateSystems(Scene scene, float delta) {

		trackTime('PhysicsSystem::movement') { ->
			movementSystem.update(scene, delta)
		}
		trackTime('PhysicsSystem::collision') { ->
			collisionSystem.update(scene, delta)
		}
	}
}
