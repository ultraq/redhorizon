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

package nz.net.ultraq.redhorizon.engine.scripts

import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A system for updating any entity scripts.
 *
 * @author Emanuel Rabina
 */
class ScriptSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(ScriptSystem)

	private final ScriptEngine scriptEngine
	private final InputEventHandler input
	private final float updateStep

	private final List<ScriptNode> scripts = new ArrayList<>()
	private float accumulatedTime = 0f

	/**
	 * Constructor, configure the script system.
	 *
	 * @param scriptEngine
	 * @param input
	 * @param updateFrequency
	 *   The rate at which script updates should occur.  As the script system is
	 *   currently used for simulating movement and physics, then all script
	 *   updates are performed at a fixed rate, decoupled from frame rate, to
	 *   prevent jank.
	 */
	ScriptSystem(ScriptEngine scriptEngine, InputEventHandler input, int updateFrequency = 100) {

		this.scriptEngine = scriptEngine
		this.input = input
		this.updateStep = 1 / updateFrequency
	}

	@Override
	void update(Scene scene, float delta) {

		average('Update', 1f, logger) { ->
			scripts.clear()
			scene.traverse(ScriptNode) { ScriptNode script ->
				scripts << script
				return true
			}

			// Perform as many fixed-step updates within the accumulated frame time
			// From: http://gafferongames.com/game-physics/fix-your-timestep/
			accumulatedTime += delta
			while (accumulatedTime > updateStep) {
				scripts.each { ScriptNode script ->
					if (script.enabled) {
						script.update(scriptEngine, input, updateStep)
					}
				}
				accumulatedTime -= updateStep
			}
		}
	}
}
