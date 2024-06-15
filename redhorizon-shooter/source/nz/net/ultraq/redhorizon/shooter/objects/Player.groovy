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

import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.nodes.Rotatable
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.shooter.Shooter

import org.joml.Vector3f
import static org.lwjgl.glfw.GLFW.*

import groovy.json.JsonSlurper
import java.util.concurrent.CompletableFuture

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements GraphicsElement, Rotatable, Temporal {

	private static final float FORWARD_SPEED = 100f
	private static final float ROTATION_SPEED = 120f
	private static final float STRAFING_SPEED = 25f

	private final Unit unit
	private final float xPosRange
	private final float yPosRange
	private final Vector3f velocity = new Vector3f()
	private float forward = 0f
	private float strafing = 0f
	private float rotation = 0f
	private long moveUpdateTimeMs

	/**
	 * Constructor, load the sprite and scripts for the player.
	 */
	Player(ResourceManager resourceManager) {

		var spriteFile = resourceManager.loadFile('heli.shp', ShpFile)
		var unitJson = getResourceAsText("nz/net/ultraq/redhorizon/classic/units/data/heli.json")
		var unitData = new JsonSlurper().parseText(unitJson) as UnitData
		unit = new Unit(spriteFile, unitData)

		var rotorJson = getResourceAsText("nz/net/ultraq/redhorizon/classic/units/data/lrotor.json")
		var rotorData = new JsonSlurper().parseText(rotorJson) as UnitData
		unit.addBody(resourceManager.loadFile('lrotor.shp', ShpFile), rotorData)
		unit.body2.transform.translate(0, 2)

		addChild(unit)

		accept { Node node ->
			node.bounds.center()
		}

		attachScript(new PlayerScript())

		xPosRange = Shooter.RENDER_RESOLUTION.width() / 2 - width / 2
		yPosRange = Shooter.RENDER_RESOLUTION.height() / 2 - height / 2
	}

	@Override
	void render(GraphicsRenderer renderer) {

		// Does nothing
		// TODO: All we want GraphicsElement for is the update() method.  Maybe that
		//       needs to be moved out to the Node class 🤔
	}

	@Override
	void setHeading(float newHeading) {

		Rotatable.super.setHeading(newHeading)
		unit.setHeading(newHeading)
	}

	@Override
	void update() {

		// TODO: It'd help if we got a frame delta in here, so update() might need
		//       to provide that.
		var moveCurrentTimeMs = currentTimeMs
		var frameDelta = (moveCurrentTimeMs - moveUpdateTimeMs) / 1000

		if (forward || strafing || rotation) {
			heading += rotation * frameDelta
			var v = velocity.set(strafing, forward, 0).mul(frameDelta).rotateZ(Math.toRadians(-heading) as float)
			var currentPosition = getPosition()
			setPosition(
				Math.clamp(currentPosition.x + v.x as float, -xPosRange, xPosRange),
				Math.clamp(currentPosition.y + v.y as float, -yPosRange, yPosRange)
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
					new KeyControl(GLFW_KEY_W, 'Move forward',
						{ ->
							forward += FORWARD_SPEED
							startMovement()
						},
						{ ->
							forward -= FORWARD_SPEED
						}
					),
					new KeyControl(GLFW_KEY_S, 'Move backward',
						{ ->
							forward -= FORWARD_SPEED
							startMovement()
						},
						{ ->
							forward += FORWARD_SPEED
						}
					),
					new KeyControl(GLFW_KEY_A, 'Move left',
						{ ->
							strafing -= STRAFING_SPEED
							startMovement()
						},
						{ ->
							strafing += STRAFING_SPEED
						}
					),
					new KeyControl(GLFW_KEY_D, 'Move right',
						{ ->
							strafing += STRAFING_SPEED
							startMovement()
						},
						{ ->
							strafing -= STRAFING_SPEED
						}
					),
					new KeyControl(GLFW_KEY_LEFT, 'Rotate left',
						{ ->
							rotation -= ROTATION_SPEED
							startMovement()
						},
						{ ->
							rotation += ROTATION_SPEED
						}
					),
					new KeyControl(GLFW_KEY_RIGHT, 'Rotate right',
						{ ->
							rotation += ROTATION_SPEED
							startMovement()
						},
						{ ->
							rotation -= ROTATION_SPEED
						}
					)
				)
			}
		}

		// TODO: Really needs to be the delta between frame updates
		private void startMovement() {

			if (!moveUpdateTimeMs) {
				moveUpdateTimeMs = currentTimeMs
			}
		}
	}
}
