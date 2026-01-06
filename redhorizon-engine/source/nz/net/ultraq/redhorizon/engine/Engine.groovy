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
 * The object responsible for running each of the registered systems on a scene.
 * Systems are currently not prioritized, so are run in the order in which they
 * are added.
 *
 * @author Emanuel Rabina
 */
class Engine {

	private static final Logger logger = LoggerFactory.getLogger(Engine)

	private final List<System> systems = new ArrayList<>()
	private Scene scene

	/**
	 * Add a system to the engine.  It will be added to the end of the list and so
	 * will be updated after all previously added systems.
	 */
	Engine addSystem(System system) {

		systems.add(system)
		return this
	}

	/**
	 * An overload of the {@code <<} operator as an alias to {@link #addSystem(System)}.
	 */
	Engine leftShift(System system) {

		return addSystem(system)
	}

	/**
	 * Remove a system from the engine.
	 */
	Engine removeSystem(System system) {

		systems.remove(system)
		return this
	}

	/**
	 * Update all of the systems in the engine.
	 */
	void update(float delta) {

		// TODO: There are a lot of scene traversal methods in each of the system
		//       updates, so introduce a more eficient way of picking out necessary
		//       components.
		average('Overall engine update', 1f, logger) { ->
			systems.each { system ->
				if (system.enabled) {
					system.update(scene, delta)
				}
			}
		}
	}

	/**
	 * Use the given scene for system updates.
	 */
	Engine withScene(Scene scene) {

		this.scene = scene
		return this
	}
}
