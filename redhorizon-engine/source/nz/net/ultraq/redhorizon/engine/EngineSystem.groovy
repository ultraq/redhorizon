/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.events.EventTarget

import groovy.transform.PackageScope

/**
 * A system provides behaviour for a component or set of components.  Systems
 * traverse a {@link Scene}, looking for the components they work with, and then
 * doing something with the data in those components.
 * <p>
 * Systems operate on their own thread, and should be careful not to step on
 * data that could be used by other systems/threads.
 *
 * @author Emanuel Rabina
 */
abstract class EngineSystem implements Runnable, EventTarget {

	private Engine engine
	protected Scene scene

	/**
	 * Configure the current scene with this system.
	 */
	abstract void configureScene()

	/**
	 * Return the engine that this system is a part of.
	 */
	protected Engine getEngine() {

		return engine
	}

	/**
	 * Execute an action and optionally wait such that, if repeated, it would run
	 * no faster than the given frequency.
	 *
	 * @param frequency
	 *   The number of times per second the action could be repeated.
	 * @param action
	 * @return
	 */
	protected static void rateLimit(float frequency, Closure action) {

		var maxExecTime = 1000f / frequency
		var execTime = time(action)
		var waitTime = maxExecTime - execTime
		if (waitTime > 0) {
			Thread.sleep((long)waitTime)
		}
	}

	/**
	 * Set the engine this system is a part of.
	 */
	@PackageScope
	void setEngine(Engine engine) {

		this.engine = engine
	}

	/**
	 * Set and configure the scene with this system
	 */
	@PackageScope
	void setScene(Scene scene) {

		this.scene = scene
		configureScene()
	}
}
