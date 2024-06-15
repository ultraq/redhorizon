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

package nz.net.ultraq.redhorizon.shooter.objects

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.filetypes.PngFile

import org.joml.Vector2f
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.CompletableFuture

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements GraphicsElement, Temporal {

	private static final float SPEED = 100f

	private final Sprite sprite
	private final Vector2f velocity = new Vector2f()
	private final float xPosRange
	private final float yPosRange
	private long moveUpdateTimeMs

	/**
	 * Constructor, load the sprite and scripts for the player.
	 */
	Player(PngFile spriteFile) {

		sprite = new Sprite(spriteFile).tap {
			bounds.center()
		}
		addChild(sprite)

		attachScript(new PlayerScript())

		xPosRange = nz.net.ultraq.redhorizon.shooter.Shooter.RENDER_RESOLUTION.width() / 2 - 12
		yPosRange = nz.net.ultraq.redhorizon.shooter.Shooter.RENDER_RESOLUTION.height() / 2 - 8
	}

	@Override
	void render(GraphicsRenderer renderer) {

		// Does nothing
		// TODO: All we want GraphicsElement for is the update() method.  Maybe that
		//       needs to be moved out to the Node class 🤔
	}

	@Override
	void update() {

		var moveCurrentTimeMs = currentTimeMs
		var frameDelta = (moveCurrentTimeMs - moveUpdateTimeMs) / 1000

		// TODO: It'd help if we got a frame delta in here, so update() might need
		//       to provide that.
		if (velocity.length()) {
			var v = new Vector2f(velocity).normalize().mul(SPEED)
			var currentPosition = getPosition()
			setPosition(
				Math.clamp(currentPosition.x + v.x * frameDelta as float, -xPosRange, xPosRange),
				Math.clamp(currentPosition.y + v.y * frameDelta as float, -yPosRange, yPosRange)
			)
		}

		moveUpdateTimeMs = moveCurrentTimeMs
	}

	/**
	 * Script for the player object.
	 */
	class PlayerScript extends Script<Player> {

//		@Delegate
//		private Player applyDelegate() {
//			return scriptable as Player
//		}
//
		@Override
		CompletableFuture<Void> onSceneAdded(Scene scene) {

			return CompletableFuture.runAsync { ->

				// TODO: Inertia and momentum
				scene.inputEventStream.addControls(
					new KeyControl(GLFW_KEY_A, 'Move left',
						{ ->
							velocity.x -= 1
							moveUpdateTimeMs = currentTimeMs
						},
						{ ->
							velocity.x += 1
						}
					),
					new KeyControl(GLFW_KEY_D, 'Move right',
						{ ->
							velocity.x += 1
							moveUpdateTimeMs = currentTimeMs
						},
						{ ->
							velocity.x -= 1
						}
					),
					new KeyControl(GLFW_KEY_W, 'Move up',
						{ ->
							velocity.y += 1
							moveUpdateTimeMs = currentTimeMs
						},
						{ ->
							velocity.y -= 1
						}
					),
					new KeyControl(GLFW_KEY_S, 'Move down',
						{ ->
							velocity.y += -1
							moveUpdateTimeMs = currentTimeMs
						},
						{ ->
							velocity.y -= -1
						}
					)
				)
			}
		}
	}
}
