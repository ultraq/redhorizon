/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.events.EventTarget

/**
 * The interface for creating your own application to run using the Red Horizon
 * engine.  An implementation of this is given to a {@link Runtime} to configure
 * and run your app.
 *
 * @author Emanuel Rabina
 */
interface Application extends EventTarget {

	/**
	 * Return the name of the application.  This is displayed in the window title,
	 * alongside the version.
	 */
	String getName()

	/**
	 * Return the version of the application.  This is displayed in the window
	 * title, alongside the name.
	 */
	String getVersion()

	/**
	 * Called by the runtime when the engine and an empty scene are set up, and
	 * passed the scene for the application to use.
	 */
	void start(Scene scene)

	/**
	 * Called by the runtime before the engine is shut down.
	 */
	void stop(Scene scene)
}
