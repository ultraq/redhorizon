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

package nz.net.ultraq.redhorizon.engine.scripting

import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneEvents

/**
 * A script is a piece of code attached to an entity to customize its behaviour.
 *
 * @author Emanuel Rabina
 */
abstract class Script<T extends Scriptable> implements SceneEvents {

	protected T scriptable

	/**
	 * Attach the script target to this script, allowing it to act as a delegate
	 * within other script methods.
	 */
	Script<T> attachScriptable(T scriptable) {

		this.scriptable = scriptable
		return this
	}

	@Override
	void onSceneAdded(Scene scene) {
	}
}
