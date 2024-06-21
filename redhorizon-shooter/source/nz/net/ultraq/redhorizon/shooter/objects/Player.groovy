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
import nz.net.ultraq.redhorizon.engine.input.GamepadControl
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.shooter.Shooter

import org.joml.Vector2f
import static org.lwjgl.glfw.GLFW.*

import groovy.json.JsonSlurper
import java.util.concurrent.Executors

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements GraphicsElement, Rotatable, Temporal {

	private static final float MOVEMENT_SPEED = 100f
	private static final float ROTATION_SPEED = 180f
	private static final Vector2f up = new Vector2f(0, 1)

	private final Unit unit
	private final float xPosRange
	private final float yPosRange

	private Vector2f velocity = new Vector2f()
	private Vector2f direction = new Vector2f()
	private Vector2f movement = new Vector2f()
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
		var frameDelta = (moveCurrentTimeMs - (moveUpdateTimeMs ?: moveCurrentTimeMs)) / 1000

		if (velocity.length()) {
			var currentPosition = getPosition()
			movement.set(velocity).normalize().mul(MOVEMENT_SPEED).mul(frameDelta)
			setPosition(
				Math.clamp(currentPosition.x + movement.x as float, -xPosRange, xPosRange),
				Math.clamp(currentPosition.y + movement.y as float, -yPosRange, yPosRange)
			)
		}

		// Keyboard rotation
		if (rotation) {
			heading = heading + rotation * ROTATION_SPEED * frameDelta as float
		}
		// Gamepad rotation
		else if (direction.length()) {
			heading = Math.toDegrees(direction.angle(up)) as float
		}

		moveUpdateTimeMs = moveCurrentTimeMs
	}

	/**
	 * Script for the player object.
	 */
	class PlayerScript extends Script<Player> {

		private boolean bobbing

		@Override
		void onSceneAdded(Scene scene) {

			bobbing = true

			// Helicopter bobbing
			Executors.newVirtualThreadPerTaskExecutor().execute { ->
				while (bobbing) {
					var bob = 0.0625 * Math.sin(currentTimeMs / 750)
					var unitPosition = unit.position
					unit.setPosition(unitPosition.x, unitPosition.y + bob as float, unitPosition.z)
					Thread.sleep(10)
				}
			}

			// TODO: Inertia and momentum

			// Keyboard controls
			scene.inputEventStream.addControls(
				new KeyControl(GLFW_KEY_W, 'Move up',
					{ -> velocity.y += 1 },
					{ -> velocity.y -= 1 }
				),
				new KeyControl(GLFW_KEY_S, 'Move down',
					{ -> velocity.y -= 1 },
					{ -> velocity.y += 1 }
				),
				new KeyControl(GLFW_KEY_A, 'Move left',
					{ -> velocity.x -= 1 },
					{ -> velocity.x += 1 }
				),
				new KeyControl(GLFW_KEY_D, 'Move right',
					{ -> velocity.x += 1 },
					{ -> velocity.x -= 1 }
				),
				new KeyControl(GLFW_KEY_LEFT, 'Rotate left',
					{ -> rotation -= 1 },
					{ -> rotation += 1 }
				),
				new KeyControl(GLFW_KEY_RIGHT, 'Rotate right',
					{ -> rotation += 1 },
					{ -> rotation -= 1 }
				)
			)

			// Gamepad controls
			scene.inputEventStream.addControls(
				new GamepadControl(GLFW_GAMEPAD_AXIS_LEFT_X, 'Movement along the X axis', { value ->
					velocity.x = value
				}),
				new GamepadControl(GLFW_GAMEPAD_AXIS_LEFT_Y, 'Movement along the Y axis', { value ->
					velocity.y = -value
				}),
				new GamepadControl(GLFW_GAMEPAD_AXIS_RIGHT_X, 'Heading along the X axis', { value ->
					direction.x = value
				}),
				new GamepadControl(GLFW_GAMEPAD_AXIS_RIGHT_Y, 'Heading along the Y axis', { value ->
					direction.y = -value
				})
			)
		}

		@Override
		void onSceneRemoved(Scene scene) {

			bobbing = false
		}
	}
}