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
import nz.net.ultraq.redhorizon.engine.EngineSystemType

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

	final EngineSystemType type = EngineSystemType.UPDATE

	private long lastUpdateTimeMs

	@Override
	void configureScene() {

		EngineStats.instance.attachGameLogicSystem(this)
	}

	@Override
	void runLoop() {

		List<GameObject> gameObjects = []

		while (!Thread.interrupted()) {
			try {
				process { ->
					var currentTimeMs = System.currentTimeMillis()
					var delta = (currentTimeMs - (lastUpdateTimeMs ?: currentTimeMs)) / 1000

					average('Updating', 1f, logger) { ->
						scene?.query(GameObject, gameObjects)*.update(delta)
						gameObjects.clear()
					}

					lastUpdateTimeMs = currentTimeMs
				}
			}
			catch (InterruptedException ignored) {
				break
			}
		}
	}
}
