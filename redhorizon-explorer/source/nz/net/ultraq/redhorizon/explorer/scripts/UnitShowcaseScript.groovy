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

package nz.net.ultraq.redhorizon.explorer.scripts

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor

/**
 * Controls a unit for showcasing in the explorer.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class UnitShowcaseScript extends Script<Unit> {

	private static final Logger logger = LoggerFactory.getLogger(UnitShowcaseScript)

	final Camera camera

	private final List<RemoveControlFunction> removeControlFunctions = []

	@Delegate
	private Unit applyDelegate() {
		return scriptable as Unit
	}

	@Override
	void onSceneAdded(Scene scene) {

		camera.setScaleXY(4)
		logger.info("Showing ${state} state")

		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_A, 'Rotate left', { ->
			rotateLeft()
		}, true))
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_D, 'Rotate right', { ->
			rotateRight()
		}, true))

		var states = unitData.shpFile.states

		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_W, 'Previous animation', { ->
			var currentStateIndex = states.findIndexOf { it.name == state }
			setState(states[Math.wrap(currentStateIndex - 1, 0, states.length)].name)
			logger.info("Showing ${state} state")
			startAnimation()
		}))
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_S, 'Next animation', { ->
			var currentStateIndex = states.findIndexOf { it.name == state }
			setState(states[Math.wrap(currentStateIndex + 1, 0, states.length)].name)
			logger.info("Showing ${state} state")
			startAnimation()
		}))
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Pause animation', { ->
//			scene.gameClock.togglePause()
		}))

		var Faction[] factions = Faction.values()
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_F, 'Cycle faction colours', {
			->
			var selectedFaction = factions[(faction.ordinal() + 1) % factions.length]
			logger.info('Viewing with {} faction colours', selectedFaction.name())
			faction = selectedFaction
		}))
	}

	@Override
	void onSceneRemoved(Scene scene) {

		removeControlFunctions*.remove()
	}
}
