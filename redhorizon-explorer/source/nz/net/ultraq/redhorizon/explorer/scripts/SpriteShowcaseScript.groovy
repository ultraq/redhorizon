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
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor
import java.util.concurrent.CompletableFuture

/**
 * Script for going forwards/backwards through a sprite files frames.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class SpriteShowcaseScript extends Script<PalettedSprite> {

	private static final Logger logger = LoggerFactory.getLogger(SpriteShowcaseScript)

	final Camera camera

	private int currentFrame
	private final List<RemoveControlFunction> removeControlFunctions = []

	@Delegate
	PalettedSprite applyDelegate() {
		return scriptable
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return CompletableFuture.runAsync { ->
			camera.setScaleXY(4)

			removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_A, 'Previous frame', { ->
				Math.wrap(currentFrame--, 0, numImages)
				region.set(spriteSheet[currentFrame])
			}))
			removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_D, 'Next frame', { ->
				Math.wrap(currentFrame++, 0, numImages)
				region.set(spriteSheet[currentFrame])
			}))

			var Faction[] factions = Faction.values()
			removeControlFunctions << scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_F, 'Cycle faction colours', {
				->
				var selectedFaction = factions[(faction.ordinal() + 1) % factions.length]
				logger.info("Viewing with ${selectedFaction.name()} faction colours")
				accept { node ->
					if (node instanceof FactionColours) {
						node.faction = selectedFaction
					}
				}
			}))
		}
	}

	@Override
	CompletableFuture<Void> onSceneRemoved(Scene scene) {

		return CompletableFuture.runAsync { ->
			camera.reset()
			removeControlFunctions*.remove()
		}
	}
}
