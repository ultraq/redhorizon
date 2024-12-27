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

package nz.net.ultraq.redhorizon.engine.physics

import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.EngineSystemStoppedEvent

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Engine system for running the physics simulation of included objects.
 *
 * @author Emanuel Rabina
 */
class PhysicsSystem extends EngineSystem {

	private static final Logger logger = LoggerFactory.getLogger(PhysicsSystem)

	private long lastUpdateTimeMs

	@Override
	void configureScene() {
	}

	@Override
	void run() {

		// Initialization
		logger.debug('Starting physics system')
		Box2D.init()
		var world = new World(new Vector2(0, -10), true)
		var debugRenderer = new Box2DDebugRenderer()

		var body = world.createBody(new BodyDef().tap {
			type = BodyType.DynamicBody
			position.set(5, 10)
		})
		var circle = new CircleShape().tap {
			radius = 6f
		}
		body.createFixture(new FixtureDef().tap {
			shape = circle
			density = 0.5f
			friction = 0.4f
			restitution = 0.6f
		})

		var groundBody = world.createBody(new BodyDef().tap {
			position.set(0, 10)
		})
		var groundBox = new PolygonShape().tap {
			setAsBox(100, 10)
		}
		groundBody.createFixture(groundBox, 0f)

		// Simulation loop
		logger.debug('Physics system in simulation loop...')
		while (!Thread.interrupted()) {
			try {
				rateLimit(100) { ->
					var currentTimeMs = System.currentTimeMillis()
					var delta = (currentTimeMs - (lastUpdateTimeMs ?: currentTimeMs)) / 1000

					average('Updating', 1f, logger) { ->
						debugRenderer.render(world,)
						world.step(delta, 6, 2)
					}

					lastUpdateTimeMs = currentTimeMs
				}
			}
			catch (InterruptedException ignored) {
				break
			}
		}

		circle.dispose()
		groundBox.dispose()
		world.dispose()

		// Shutdown
		logger.debug('Shutting down physics system')
		trigger(new EngineSystemStoppedEvent())
		logger.debug('Physics system stopped')
	}
}
