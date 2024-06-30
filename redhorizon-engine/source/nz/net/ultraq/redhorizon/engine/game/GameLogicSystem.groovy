/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.game

import nz.net.ultraq.redhorizon.engine.EngineStats
import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.SystemReadyEvent
import nz.net.ultraq.redhorizon.engine.SystemStoppedEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.scenegraph.Node

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Engine subsystem for running game logic and updating objects in response to
 * inputs and other factors.
 *
 * @author Emanuel Rabina
 */
class GameLogicSystem extends EngineSystem {

	private static final Logger logger = LoggerFactory.getLogger(GameLogicSystem)

	private long lastUpdateTimeMs

	@Override
	void configureScene() {

		EngineStats.instance.attachGameLogicSystem(this)
	}

	@Override
	void run() {

		Thread.currentThread().name = 'Game Logic System'
		logger.debug('Starting game logic system')

		// Initialization
		// TODO: These phases could probably live in the parent EngineSystem class
		var graphicsSystem = (GraphicsSystem)engine.systems.find { system -> system instanceof GraphicsSystem }
		trigger(new SystemReadyEvent())

		// Game loop
		logger.debug('Game logic system in loop...')
		while (!Thread.interrupted()) {
			try {
				var currentTimeMs = System.currentTimeMillis()
				var delta = (currentTimeMs - (lastUpdateTimeMs ?: currentTimeMs)) / 1000

				average('Updating', 1f, logger) { ->
					scene?.traverse { Node node ->
						node.update(delta)
						node.script?.update(delta)
						return true
					}
				}
				graphicsSystem.waitForContinue()

				lastUpdateTimeMs = currentTimeMs
			}
			catch (InterruptedException ignored) {
				break
			}
		}

		// Shutdown
		logger.debug('Shutting down game logic system')

		trigger(new SystemStoppedEvent())
		logger.debug('Game logic system stopped')
	}
}