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

import nz.net.ultraq.redhorizon.classic.nodes.FactionColours
import nz.net.ultraq.redhorizon.classic.units.Faction
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.explorer.objects.Unit

import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor

/**
 * Controls a unit for showcasing in the explorer.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class UnitShowcaseScript extends Script<Unit> {

	final UnitData unitData

	private final List<RemoveControlFunction> removeControlFunctions = []

	@Delegate
	private Unit applyDelegate() {
		return scriptable as Unit
	}

	@Override
	void onSceneAdded(Scene scene) {

		var states = unitData.shpFile.states?.size() ?: 0
		var stateIndex = -1

		// TODO: Have it so that the render window is the desktop resolution and the
		//       camera scales things so that things are the size they were back
		//       when the game was 640x480
		scene.camera.scale(2.0f)

		// Adjust the heading of the unit such that it's rotated left enough to
		// utilize its next state/animation in that direction.
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_LEFT, 'Rotate left', { ->
			var headings = stateIndex == -1 ?
				unitData.shpFile.parts.body.headings :
				unitData.shpFile.states[stateIndex].headings
			var degreesPerHeading = (360f / headings) as float
			heading -= degreesPerHeading
		}))

		// Adjust the heading of the unit such that it's rotated right enough to
		// utilize its next state/animation in that direction.
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_RIGHT, 'Rotate right', { ->
			var headings = stateIndex == -1 ?
				unitData.shpFile.parts.body.headings :
				unitData.shpFile.states[stateIndex].headings
			var degreesPerHeading = (360f / headings) as float
			heading += degreesPerHeading
		}))

//		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_UP, 'Previous animation', { ->
//			previousState()
//			startAnimation()
//			stateIndex = Math.wrap(stateIndex - 1, -1, states)
//		}))
//		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_DOWN, 'Next animation', { ->
//			nextState()
//			startAnimation()
//			stateIndex = Math.wrap(stateIndex + 1, -1, states)
//		}))
//		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Pause animation', { ->
//			scene.gameClock.togglePause()
//		}))

		var Faction[] factions = Faction.values()
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_P, 'Cycle faction colours', { ->
			var selectedFaction = factions[(faction.ordinal() + 1) % factions.length]
			accept { node ->
				if (node instanceof FactionColours) {
					node.faction = selectedFaction
				}
			}
		}))
	}
}
