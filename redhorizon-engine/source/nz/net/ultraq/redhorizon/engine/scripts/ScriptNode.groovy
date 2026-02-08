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

package nz.net.ultraq.redhorizon.engine.scripts

import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Node

/**
 * A wrapper for scripts that allows them to be a part of the scene and operate
 * on their parent node.
 *
 * @author Emanuel Rabina
 */
class ScriptNode extends Node<ScriptNode> implements AutoCloseable {

	final String scriptName
	final Class<? extends Script> scriptClass
	private Script script

	/**
	 * Constructor, set the script used to a Groovy file on the engine's script
	 * path.
	 */
	ScriptNode(String scriptName) {

		this.scriptName = scriptName
		this.scriptClass = null
	}

	/**
	 * Constructor, set the script used to a Groovy class.
	 */
	ScriptNode(Class<? extends Script> scriptClass) {

		this.scriptClass = scriptClass
		this.scriptName = null
	}

	@Override
	void close() {

		if (script instanceof AutoCloseable) {
			script.close()
		}
	}

	/**
	 * Return the script instance.
	 */
	Script getScript() {

		return script
	}

	/**
	 * Perform any logic as part of the scene update.
	 */
	void update(ScriptEngine scriptEngine, InputEventHandler input, float delta) {

		if (scriptName) {
			def (scriptObject, isNew) = scriptEngine.loadScriptClass(scriptName, this)
			if (isNew) {
				script = scriptObject as Script
				script.entity = parent
				script.input = input
				script.init()
			}
		}
		else if (!script && scriptClass) {
			script = scriptClass.getDeclaredConstructor().newInstance()
			script.entity = parent
			script.input = input
			script.init()
		}
		script.update(delta)
	}
}
