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
import nz.net.ultraq.redhorizon.classic.nodes.Layer
import nz.net.ultraq.redhorizon.classic.nodes.Rotatable
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.engine.game.Command
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.GamepadControl
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.PartitionHint
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.engine.scenegraph.UpdateHint
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.explorer.animation.EasingFunctions
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements Rotatable, Temporal {

	private static final Logger logger = LoggerFactory.getLogger(Player)
	private static final Rectanglef MOVEMENT_RANGE = Map.MAX_BOUNDS
	private static final float MAX_SPEED = 200f
	private static final float TIME_TO_MAX_SPEED_MS = 1000 // ms
	private static final float TIME_TO_STOP_MS = 2000
	private static final float ROTATION_SPEED = 180f
	private static final Vector2f up = new Vector2f(0, 1)

	final PartitionHint partitionHint = PartitionHint.NONE
	final UpdateHint updateHint = UpdateHint.ALWAYS
	private final Unit unit

	private boolean keyboardForwards
	private boolean keyboardBackwards
	private boolean keyboardStrafeLeft
	private boolean keyboardStrafeRight
	private final Vector2f screenPosition = new Vector2f()
	private Vector2f impulse = new Vector2f()
	private long startAccelerationTimeMs
	private long stopAccelerationTimeMs
	private Vector2f velocity = new Vector2f()
	private Vector2f initialStoppingVelocity = new Vector2f()
	private Vector2f direction = new Vector2f()
	private Vector2f movement = new Vector2f()
	private Vector2f lookAt = new Vector2f()
	private Vector2f lastLookAt = new Vector2f()
	private Vector2f relativeLookAt = new Vector2f()
	private float rotation = 0f

	private boolean firing
	private List<Command> firingCommands = new CopyOnWriteArrayList<>()

	private final long rateOfFireMs = 1000
	private ScheduledFuture<?> firingTask
	private final ImagesFile bulletImagesFile
	private ScheduledExecutorService firingService = Executors.newScheduledThreadPool(4)

	/**
	 * Constructor, load the sprite and scripts for the player.
	 */
	Player(ResourceManager resourceManager) {

		unit = new Unit(resourceManager.loadFile('heli.shp', ShpFile), getUnitData('heli')).tap {
			layer = Layer.UP_ONE
		}

		unit.addBody(resourceManager.loadFile('lrotor.shp', ShpFile), getUnitData('lrotor'))
		unit.body2.transform { ->
			translate(0, 2)
		}

		unit.addShadow()
		addChild(unit)

		traverse { Node node ->
			node.bounds { ->
				center()
			}
			return true
		}

//		bulletImagesFile = resourceManager.loadFile('dragon.shp', ShpFile)

		attachScript(new PlayerScript())
	}

	@Override
	void setHeading(float newHeading) {

		Rotatable.super.setHeading(newHeading)
		unit.setHeading(newHeading)
	}

	@Override
	void update(float delta) {

		// TODO: The separate input handling should probably be split up so it's not all jumbled together

		// Apply keyboard movement
		if (keyboardForwards) {
			var headingInRadians = Math.toRadians(heading)
			impulse.set(Math.sin(headingInRadians), Math.cos(headingInRadians)).normalize()
		}
		if (keyboardBackwards) {
			var headingInRadians = Math.toRadians(heading)
			impulse.set(Math.sin(headingInRadians), Math.cos(headingInRadians)).negate().normalize()
		}
		if (keyboardStrafeLeft) {
			var leftAngle = Math.wrap((float)(heading - 90f), 0f, 360f)
			var leftAngleInRadians = Math.toRadians(leftAngle)
			impulse.set(Math.sin(leftAngleInRadians), Math.cos(leftAngleInRadians)).normalize()
		}
		if (keyboardStrafeRight) {
			var rightAngle = Math.wrap((float)(heading + 90f), 0f, 360f)
			var rightAngleInRadians = Math.toRadians(rightAngle)
			impulse.set(Math.sin(rightAngleInRadians), Math.cos(rightAngleInRadians)).normalize()
		}
		if (!keyboardForwards && !keyboardBackwards && !keyboardStrafeLeft && !keyboardStrafeRight) {
			impulse.zero()
		}

		// Movement
		if (impulse) {
			startAccelerationTimeMs ?= currentTimeMs
			if (stopAccelerationTimeMs) {
				stopAccelerationTimeMs = 0
			}
			if (initialStoppingVelocity) {
				initialStoppingVelocity.zero()
			}
			var timeElapsedMs = Math.min(currentTimeMs - startAccelerationTimeMs, TIME_TO_MAX_SPEED_MS)
			var accelerationCurve = EasingFunctions.linear((float)(timeElapsedMs / TIME_TO_MAX_SPEED_MS))
			var maxAcceleration = new Vector2f(impulse).mul(MAX_SPEED).mul(accelerationCurve).mul(delta)
			velocity.set(maxAcceleration)
		}
		else if (velocity) {
			stopAccelerationTimeMs ?= currentTimeMs
			startAccelerationTimeMs = 0
			if (!initialStoppingVelocity) {
				initialStoppingVelocity.set(velocity)
			}
			var timeElapsedMs = Math.min(currentTimeMs - stopAccelerationTimeMs, TIME_TO_STOP_MS)
			var deccelerationCurve = EasingFunctions.linear((float)(1 - timeElapsedMs / TIME_TO_STOP_MS))
			var maxDecceleration = new Vector2f(initialStoppingVelocity).mul(deccelerationCurve)
			velocity.set(maxDecceleration)
		}
		if (velocity) {
			movement.set(position).add(velocity)
			setPosition(
				Math.clamp(movement.x, MOVEMENT_RANGE.minX, MOVEMENT_RANGE.maxX),
				Math.clamp(movement.y, MOVEMENT_RANGE.minY, MOVEMENT_RANGE.maxY)
			)
		}

		// Mouse rotation
		if (lookAt.length() && lookAt != lastLookAt) {
			scene.camera.calculateSceneToScreenSpace(position, screenPosition)
			heading = Math.toDegrees(relativeLookAt.set(lookAt).sub(screenPosition.x(), screenPosition.y()).angle(up)) as float

			// Adjust lookAt position with movement
			lookAt.add(movement)
			lastLookAt.set(lookAt)
		}
		// Keyboard rotation
		else if (rotation) {
			heading = heading + rotation * ROTATION_SPEED * delta as float
		}
		// Gamepad rotation
		else if (direction.length()) {
			heading = Math.toDegrees(direction.angle(up)) as float
		}
		// Let the current movement direction inform the heading
//		else if (velocity.length()) {
//			heading = Math.toDegrees(velocity.angle(up)) as float
//		}

		// Gamepad firing
		if (firing) {
			logger.debug('Firing')
			firingTask = firingService.scheduleAtFixedRate({ ->
				var bullet = new Bullet(bulletImagesFile, heading)
				bullet.position = globalPosition
				addChild(bullet)
			}, 0, rateOfFireMs, TimeUnit.MILLISECONDS)
		}
		else {
			firingTask?.cancel()
		}
	}

	/**
	 * Script for the player object.
	 */
	class PlayerScript extends Script<Player> {

		private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2)
		private ScheduledFuture<?> bobbingTask

		@Override
		void onSceneAdded(Scene scene) {

			// Helicopter bobbing
			bobbingTask = scheduledExecutor.scheduleAtFixedRate({ ->
				var bob = 0.0625 * Math.sin(currentTimeMs / 750) as float
				unit.body.transform { ->
					translate(0f, bob, 0f)
				}
				unit.body2.transform { ->
					translate(0f, bob, 0f)
				}
			}, 0, 10, TimeUnit.MILLISECONDS)

			// Keyboard controls
			scene.inputEventStream.addControls(
				new KeyControl(GLFW_KEY_W, 'Move forwards',
					{ -> keyboardForwards = true },
					{ -> keyboardForwards = false }
				),
				new KeyControl(GLFW_KEY_S, 'Move backwards',
					{ -> keyboardBackwards = true },
					{ -> keyboardBackwards = false }
				),
				new KeyControl(GLFW_KEY_A, 'Strafe left',
					{ -> keyboardStrafeLeft = true },
					{ -> keyboardStrafeLeft = false }
				),
				new KeyControl(GLFW_KEY_D, 'Strafe right',
					{ -> keyboardStrafeRight = true },
					{ -> keyboardStrafeRight = false }
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
					.set(event.xPos, scene.window.size.height() - event.yPos)
					.div(scene.window.renderToWindowScale)
			}

			// Gamepad controls
			scene.inputEventStream.addControls(
				new GamepadControl(GLFW_GAMEPAD_AXIS_LEFT_X, 'Movement along the X axis',
					{ value -> velocity.x = value }
				),
				new GamepadControl(GLFW_GAMEPAD_AXIS_LEFT_Y, 'Movement along the Y axis',
					{ value -> velocity.y = -value }
				),
				new GamepadControl(GLFW_GAMEPAD_AXIS_RIGHT_X, 'Heading along the X axis',
					{ value -> direction.x = value }
				),
				new GamepadControl(GLFW_GAMEPAD_AXIS_RIGHT_Y, 'Heading along the Y axis',
					{ value -> direction.y = -value }
				),
				new GamepadControl(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER, 'Fire',
					{ value -> firing = value > -1f }
				)
			)
		}

		@Override
		void onSceneRemoved(Scene scene) {

			bobbingTask.cancel()
		}
	}
}
