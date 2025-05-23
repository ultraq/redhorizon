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
import nz.net.ultraq.redhorizon.engine.game.GameObject
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.GamepadControl
import nz.net.ultraq.redhorizon.engine.input.InputEvent
import nz.net.ultraq.redhorizon.engine.input.InputHandler
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.PartitionHint
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
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
class Player extends Node<Player> implements GameObject, InputHandler, Rotatable {

	private static final Logger logger = LoggerFactory.getLogger(Player)
	private static final Rectanglef MOVEMENT_RANGE = Map.MAX_BOUNDS
	private static final float MAX_SPEED = 200f
	private static final float TIME_TO_MAX_SPEED_S = 1
	private static final float ROTATION_SPEED = 180f
	private static final Vector2f up = new Vector2f(0, 1)

	final PartitionHint partitionHint = PartitionHint.NONE
	private final Unit unit

	private boolean keyboardForwards
	private boolean keyboardBackwards
	private boolean keyboardStrafeLeft
	private boolean keyboardStrafeRight
	private final Vector2f screenPosition = new Vector2f()
	private Vector2f impulse = new Vector2f()
	private Vector2f velocity = new Vector2f()
	private Vector2f initialStoppingVelocity = new Vector2f()
	private Vector2f direction = new Vector2f()
	private Vector2f movement = new Vector2f()
	private Vector2f lookAt = new Vector2f()
	private Vector2f lastLookAt = new Vector2f()
	private Vector2f relativeLookAt = new Vector2f()
	private float rotation = 0f
	private float accAccelerationTime = 0f
	private float accTime

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
	boolean input(InputEvent inputEvent) {

		if (inputEvent instanceof KeyEvent) {
			return switch (inputEvent.action) {
				case GLFW_PRESS -> {
					yield switch (inputEvent.key) {
						case GLFW_KEY_W -> inputHandled { -> keyboardForwards = true }
						case GLFW_KEY_S -> inputHandled { -> keyboardBackwards = true }
						case GLFW_KEY_A -> inputHandled { -> keyboardStrafeLeft = true }
						case GLFW_KEY_D -> inputHandled { -> keyboardStrafeRight = true }
						case GLFW_KEY_LEFT -> inputHandled { -> rotation -= 1 }
						case GLFW_KEY_RIGHT -> inputHandled { -> rotation += 1 }
						default -> false
					}
				}
				case GLFW_RELEASE -> {
					yield switch (inputEvent.key) {
						case GLFW_KEY_W -> inputHandled { -> keyboardForwards = false }
						case GLFW_KEY_S -> inputHandled { -> keyboardBackwards = false }
						case GLFW_KEY_A -> inputHandled { -> keyboardStrafeLeft = false }
						case GLFW_KEY_D -> inputHandled { -> keyboardStrafeRight = false }
						case GLFW_KEY_LEFT -> inputHandled { -> rotation += 1 }
						case GLFW_KEY_RIGHT -> inputHandled { -> rotation -= 1 }
						default -> false
					}
				}
			}
		}

		if (inputEvent instanceof CursorPositionEvent) {
			inputHandled { ->
				lookAt
					.set(inputEvent.xPos, scene.window.size.height() - inputEvent.yPos)
					.div(scene.window.renderToWindowScale)
			}
		}

		return false
	}

	@Override
	void setHeading(float newHeading) {

		Rotatable.super.setHeading(newHeading)
		unit.setHeading(newHeading)
	}

	@Override
	void update(float delta) {

		accTime += delta

		// TODO: The separate input handling should probably be split up so it's not all jumbled together

		// Apply keyboard movement
		float impulseDirection = 0f
		var accelerating = false
		if (keyboardForwards) {
			impulseDirection =
				keyboardStrafeLeft ? Math.wrapToCircle((float)(heading - 45f)) :
					keyboardStrafeRight ? Math.wrapToCircle((float)(heading + 45f)) :
						heading
			accelerating = true
		}
		else if (keyboardBackwards) {
			impulseDirection =
				keyboardStrafeLeft ? Math.wrapToCircle((float)(heading + 180f + 45f)) :
					keyboardStrafeRight ? Math.wrapToCircle((float)(heading + 180f - 45f)) :
						Math.wrapToCircle((float)(heading - 180f))
			accelerating = true
		}
		else if (keyboardStrafeLeft) {
			impulseDirection = Math.wrapToCircle((float)(heading - 90f))
			accelerating = true
		}
		else if (keyboardStrafeRight) {
			impulseDirection = Math.wrapToCircle((float)(heading + 90f))
			accelerating = true
		}

		if (accelerating) {
			var headingInRadians = Math.toRadians(impulseDirection)
			impulse.set(Math.sin(headingInRadians), Math.cos(headingInRadians)).normalize()
			accAccelerationTime = Math.min((float)(accAccelerationTime + delta), TIME_TO_MAX_SPEED_S)
		}
		else {
			impulse.zero()
			accAccelerationTime = Math.max((float)(accAccelerationTime - delta), 0f)
		}

		// Movement
		if (impulse) {
			if (initialStoppingVelocity) {
				initialStoppingVelocity.zero()
			}
			var accelerationCurve = EasingFunctions.linear(accAccelerationTime)
			var maxAcceleration = new Vector2f(impulse).mul(MAX_SPEED).mul(accelerationCurve).mul(delta)
			velocity.set(maxAcceleration)
		}
		else if (velocity) {
			if (!initialStoppingVelocity) {
				initialStoppingVelocity.set(velocity)
			}
			var deccelerationCurve = EasingFunctions.linear(accAccelerationTime)
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
				var bob = 0.0625 * Math.sin(accTime) as float
				unit.body.transform { ->
					translate(0f, bob, 0f)
				}
				unit.body2.transform { ->
					translate(0f, bob, 0f)
				}
			}, 0, 10, TimeUnit.MILLISECONDS)

			// Gamepad controls
			scene.inputRequestHandler.addControls(
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
