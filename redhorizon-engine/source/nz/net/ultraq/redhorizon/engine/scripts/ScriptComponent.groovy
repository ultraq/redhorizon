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
/**
 * Perform the logic written in the provided entity script.
 *
 * @author Emanuel Rabina
 */
class ScriptComponent extends GameLogicComponent<ScriptComponent> {

	private final ScriptEngine scriptEngine
	private final String scriptName
	private final Map<String, Object> extraProperties

	/**
	 * Constructor, set the script engine, the script to have it keep tabs on for
	 * dynamic reloading, and any additional properties on the script to set.
	 */
	ScriptComponent(ScriptEngine scriptEngine, String scriptName, Map<String, Object> extraProperties = null) {

		this.scriptEngine = scriptEngine
		this.scriptName = scriptName
		this.extraProperties = extraProperties
	}

	@Override
	void update(float delta) {

		var script = scriptEngine.loadScriptClass(scriptName) as EntityScript
		script.entity = parent

		// TODO: These extra properties could probably be defined w/ @Inject so
		//       added during script creation in ScriptEngine
		extraProperties?.each { key, value ->
			script[key] = value
		}

		script.update(delta)
	}
}
