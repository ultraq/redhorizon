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

package nz.net.ultraq.redhorizon.runtime

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.scenegraph.Scene

import groovy.transform.TupleConstructor

/**
 * The starting point for creating an application using the Red Horizon engine.
 * An implementation of this is given to a {@link Runtime} to configure and run
 * the application.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
abstract class Application implements EventTarget<Application> {

	/**
	 * Application name.  This is displayed in the window title, alongside the version.
	 */
	final String name

	/**
	 * Application version.  This is displayed in the window title, alongside the name.
	 */
	final String version

	/**
	 * Configure the engine to use in the application.
	 *
	 * @param engine
	 *   The provided engine will already come with every system added.
	 */
	protected Engine configureEngine(Engine engine) {

		return engine
	}

	/**
	 * Configure the resource manager to use in the application.
	 *
	 * @param resourceManager
	 *   The provided resource manager will already have a classpath resource
	 *   resolver with a default prefix of the application's package, or the value
	 *   set in the runtime's {@code resourceManagerPathPrefix} property.
	 */
	protected ResourceManager configureResourceManager(ResourceManager resourceManager) {

		return resourceManager
	}

	/**
	 * Configure the scene to use in the application.
	 *
	 * @param scene
	 *   The provided scene will already come with a camera and a host of
	 *   debugging elements that are initially disabled.
	 */
	protected Scene configureScene(Scene scene) {

		return scene
	}
}
