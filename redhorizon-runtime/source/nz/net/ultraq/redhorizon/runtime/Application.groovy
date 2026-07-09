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
	 * Configure the scene to use in the application.  Override to provide your
	 * own scene.
	 *
	 * @param scene
	 *   The scene to configure.  The provided scene will already come with a
	 *   camera and a host of debugging elements that are initially disabled.
	 * @return
	 *   The configured scene.
	 */
	protected Scene configureScene(Scene scene) {

		return scene
	}
}
