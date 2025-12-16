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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A small wrapper around the {@link GroovyScriptEngine} to also cater for the
 * instantiation of script classes.
 *
 * @author Emanuel Rabina
 */
class ScriptEngine {

	private static final Logger logger = LoggerFactory.getLogger(ScriptEngine)

	private final GroovyScriptEngine scriptEngine
	private final Map<String, Object> scripts = [:]

	/**
	 * Constructor, creates a new script engine.
	 *
	 * @param url
	 *   The path root from which to search for scripts.
	 */
	ScriptEngine(String url) {

		scriptEngine = new GroovyScriptEngine(url)
	}

	/**
	 * Loads and instantiates a script class if it hasn't already been
	 * instantiated.  If called and the script has not changed, then the existing
	 * class instance will be returned.  Only when a script has changed will the
	 * script class be recreated.
	 */
	Object loadScriptClass(String scriptName) {

		var script = scripts[scriptName]
		var scriptClass = scriptEngine.loadScriptByName(scriptName)
		if (!script || script.class != scriptClass) {
			if (!script) {
				logger.debug('Loading script {} for the first time', scriptName)
			}
			else {
				logger.debug('Script {} has changed, reloading', scriptName)
			}
			script = scriptClass.getDeclaredConstructor().newInstance()
			scripts[scriptName] = script
		}
		return script
	}
}
