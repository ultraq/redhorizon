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

import nz.net.ultraq.redhorizon.engine.Component
import nz.net.ultraq.redhorizon.input.InputEventHandler

/**
 * Perform the logic written in the provided entity script.
 *
 * @author Emanuel Rabina
 */
class ScriptComponent extends Component<ScriptComponent> implements AutoCloseable {

	final String scriptName
	final Class<? extends EntityScript> scriptClass
	private EntityScript script

	/**
	 * Constructor, set the script used to a Groovy file on the engine's script
	 * path.
	 */
	ScriptComponent(String scriptName) {

		this.scriptName = scriptName
		this.scriptClass = null
	}

	/**
	 * Constructor, set the script used to a Groovy class.
	 */
	ScriptComponent(Class<? extends EntityScript> scriptClass) {

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
	EntityScript getScript() {

		return script
	}

	/**
	 * Perform any logic as part of the scene update.
	 */
	void update(ScriptEngine scriptEngine, InputEventHandler input, float delta) {

		if (scriptName) {
			def (scriptObject, isNew) = scriptEngine.loadScriptClass(scriptName, this)
			if (isNew) {
				script = scriptObject as EntityScript
				script.entity = entity
				script.input = input
				script.init()
			}
		}
		else if (!script && scriptClass) {
			script = scriptClass.getDeclaredConstructor().newInstance()
			script.entity = entity
			script.input = input
			script.init()
		}
		script.update(delta)
	}
}
