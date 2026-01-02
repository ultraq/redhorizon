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

/**
 * Any object that can be added to the {@link Engine} to perform some operation
 * on a {@link Scene}.
 *
 * @author Emanuel Rabina
 */
abstract class System implements Disableable<System> {

	/**
	 * Perform the role of the system over the given scene.
	 *
	 * @param scene
	 * @param delta
	 *   Time elapsed, in seconds, since the last update.
	 */
	abstract void update(Scene scene, float delta)
}
