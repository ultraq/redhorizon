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

package nz.net.ultraq.redhorizon.engine

import nz.net.ultraq.redhorizon.scenegraph.Node

/**
 * Any object in the scene that should be updated periodically.
 *
 * @author Emanuel Rabina
 */
class GameObject<TGameObject extends GameObject> extends Node<TGameObject> {

	private String scriptName

	/**
	 * Called regularly to perform any processing as a response to changes in the
	 * scene.  This default implementation will call the configured script, if
	 * any.
	 *
	 * @param delta
	 *   Time, in seconds, since the last time this method was called.
	 * @param context
	 */
	void update(float delta, GameContext context) {

		if (scriptName) {
			var script = context.scriptEngine().loadScriptClass(scriptName) as GameObjectScript
			script.update(this, delta, context)
		}
	}

	/**
	 * Use the given script to add behaviour to this object.
	 */
	TGameObject withScript(String scriptName) {

		this.scriptName = scriptName
		return (TGameObject)this
	}
}
