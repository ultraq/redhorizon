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

package nz.net.ultraq.redhorizon.engine

import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A system for running world simulation systems at a fixed rate, decoupled from
 * frame rate, to prevent jank.
 *
 * @author Emanuel Rabina
 */
class SimulationSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(SimulationSystem)

	private final List<System> systems = []
	private final float updateStep
	private float accumulatedTime = 0f

	/**
	 * Constructor, configure the simulation system.
	 *
	 * @param updateFrequency
	 *   The rate at which simulation updates should occur
	 */
	SimulationSystem(int updateFrequency) {

		updateStep = 1 / updateFrequency
	}

	/**
	 * Add an update system to be managed by this simulation system.
	 */
	SimulationSystem addSystem(System system) {

		systems << system
		return this
	}

	/**
	 * Shorthand for {@link #addSystem}.
	 */
	SimulationSystem leftShift(System system) {

		return addSystem(system)
	}

	@Override
	void update(Scene scene, float delta) {

		average('Update', 1f, logger) { ->
			// Perform as many fixed-step updates within the accumulated frame time
			// From: http://gafferongames.com/game-physics/fix-your-timestep/
			accumulatedTime += delta
			while (accumulatedTime > updateStep) {
				systems.each { system ->
					system.update(scene, updateStep)
				}
				accumulatedTime -= updateStep
			}
		}
	}
}
