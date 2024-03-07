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
import nz.net.ultraq.redhorizon.classic.nodes.FactionColours
import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * Script for going forwards/backwards through a sprite files frames.
 *
 * @author Emanuel Rabina
 */
class SpriteShowcaseScript extends Script<PalettedSprite> {

	private static final Logger logger = LoggerFactory.getLogger(SpriteShowcaseScript)

	private int currentFrame
	private final List<RemoveControlFunction> removeControlFunctions = []

	@Delegate
	PalettedSprite applyDelegate() {
		return scriptable
	}

	void delete(GraphicsRenderer renderer) {
	}

	void init(GraphicsRenderer renderer) {
	}

	@Override
	void onSceneAdded(Scene scene) {

		scene.camera.scale(4.0f)

		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_LEFT, 'Previous frame', { ->
			Math.wrap(currentFrame--, 0, numImages)
			region.set(spriteSource.spriteSheet[currentFrame])
		}))
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_RIGHT, 'Next frame', { ->
			Math.wrap(currentFrame++, 0, numImages)
			region.set(spriteSource.spriteSheet[currentFrame])
		}))

		var Faction[] factions = Faction.values()
		removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_P, 'Cycle faction colours', { ->
			var selectedFaction = factions[(faction.ordinal() + 1) % factions.length]
			logger.info("Viewing with ${selectedFaction.name()} faction colours")
			accept { node ->
				if (node instanceof FactionColours) {
					node.faction = selectedFaction
				}
			}
		}))
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.camera.resetScale()
		removeControlFunctions*.remove()
	}
}
