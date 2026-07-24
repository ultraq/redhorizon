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

	private final List<System> systems = []
	private final float updateStep
	private float accumulatedTime = 0f

	/**
	 * Constructor, configure the physics system.
	 *
	 * @param updateFrequency
	 *   The rate at which physics updates should occur
	 */
	PhysicsSystem(int updateFrequency) {

		updateStep = updateFrequency ? 1 / updateFrequency : 0f
	}

	/**
	 * Add a system to be managed by the physics system.
	 */
	PhysicsSystem addSystem(System system) {

		systems << system
		return this
	}

	/**
	 * Shorthand for {@link #addSystem}.
	 */
	PhysicsSystem leftShift(System system) {

		return addSystem(system)
	}

	@Override
	void update(Scene scene, float delta) {

		average('Update: {}ms', 1f, logger) { ->
			// Perform as many fixed-step updates within the accumulated frame time
			// From: http://gafferongames.com/game-physics/fix-your-timestep/
			if (updateStep) {
				accumulatedTime += delta
				while (accumulatedTime > updateStep) {
					systems*.update(scene, updateStep)
					accumulatedTime -= updateStep
				}
			}
			// Run updates at the speed of the framerate
			else {
				systems*.update(scene, delta)
			}
		}
	}
}
