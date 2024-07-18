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
import nz.net.ultraq.redhorizon.classic.maps.Map
import nz.net.ultraq.redhorizon.classic.nodes.Rotatable
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.game.Command
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.GamepadControl
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script

import org.joml.Math
import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import static org.lwjgl.glfw.GLFW.*

import groovy.json.JsonSlurper
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements Rotatable, Temporal {

	private static final Rectanglef MOVEMENT_RANGE = Map.MAX_BOUNDS
	private static final float MOVEMENT_SPEED = 100f
	private static final float ROTATION_SPEED = 180f
	private static final Vector2f up = new Vector2f(0, 1)

	private final Unit unit

	private Vector2f velocity = new Vector2f()
	private Vector2f direction = new Vector2f()
	private Vector2f movement = new Vector2f()
	private Vector2f lookAt = new Vector2f()
	private Vector2f relativeLookAt = new Vector2f()
	private float rotation = 0f
	private List<Command> movementCommands = new CopyOnWriteArrayList<>()

	private final Command moveForward = { ->
		var headingInRadians = Math.toRadians(heading)
		velocity.set(Math.sin(headingInRadians), Math.cos(headingInRadians)).normalize()
	}
	private final Command moveBackwards = { ->
		var headingInRadians = Math.toRadians(heading)
		velocity.set(Math.sin(headingInRadians), Math.cos(headingInRadians)).negate().normalize()
	}
	private final Command moveLeft = { ->
		var leftAngle = java.lang.Math.wrap((float)(heading - 90f), 0f, 360f)
		var leftAngleInRadians = Math.toRadians(leftAngle)
		velocity.set(Math.sin(leftAngleInRadians), Math.cos(leftAngleInRadians)).normalize()
	}
	private final Command moveRight = { ->
		var rightAngle = java.lang.Math.wrap((float)(heading + 90f), 0f, 360f)
		var rightAngleInRadians = Math.toRadians(rightAngle)
		velocity.set(Math.sin(rightAngleInRadians), Math.cos(rightAngleInRadians)).normalize()
	}
	private final Command stop = { ->
		velocity.set(0, 0)
	}

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
		unit.body2.transform { ->
			translate(0, 2)
		}

		addChild(unit)

		traverse { Node node ->
			node.bounds { ->
				center()
			}
			return true
		}

		attachScript(new PlayerScript())
	}

	@Override
	void setHeading(float newHeading) {

		Rotatable.super.setHeading(newHeading)
		unit.setHeading(newHeading)
	}

	@Override
	void update(float delta) {

		if (movementCommands) {
			movementCommands*.execute()
		}
		else {
			stop.execute()
		}

		if (velocity.length()) {
			movement.set(velocity).normalize().mul(MOVEMENT_SPEED).mul(delta)
			setPosition(
				Math.clamp(position.x() + movement.x as float, MOVEMENT_RANGE.minX, MOVEMENT_RANGE.maxX),
				Math.clamp(position.y() + movement.y as float, MOVEMENT_RANGE.minY, MOVEMENT_RANGE.maxY)
			)
		}

		// Mouse rotation
		if (lookAt.length()) {
			heading = Math.toDegrees(relativeLookAt.set(lookAt).sub(position.x(), position.y()).angle(up))
		}
		// Keyboard rotation
		else if (rotation) {
			heading = heading + rotation * ROTATION_SPEED * delta as float
		}
		// Gamepad rotation
		else if (direction.length()) {
			heading = Math.toDegrees(direction.angle(up)) as float
		}
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
					unit.setPosition(unit.position.x(), unit.position.y() + bob as float, unit.position.z())
					Thread.sleep(10)
				}
			}

			// TODO: Inertia and momentum

			// Keyboard controls
			scene.inputEventStream.addControls(
				new KeyControl(GLFW_KEY_W, 'Move forwards',
					{ -> movementCommands.add(moveForward) },
					{ -> movementCommands.remove(moveForward) }
				),
				new KeyControl(GLFW_KEY_S, 'Move backwards',
					{ -> movementCommands.add(moveBackwards) },
					{ -> movementCommands.remove(moveBackwards) }
				),
				new KeyControl(GLFW_KEY_A, 'Strafe left',
					{ -> movementCommands.add(moveLeft) },
					{ -> movementCommands.remove(moveLeft) }
				),
				new KeyControl(GLFW_KEY_D, 'Strafe right',
					{ -> movementCommands.add(moveRight) },
					{ -> movementCommands.remove(moveRight) }
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

			// Mouse input
			scene.inputEventStream.on(CursorPositionEvent) { event ->
				lookAt
					.set(
						(float)(event.xPos - scene.window.size.width() / 2),
						(float)((scene.window.size.height() - event.yPos) - scene.window.size.height() / 2)
					)
					.div(scene.window.renderToWindowScale)
					.add(globalPosition.x(), globalPosition.y())
			}

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
