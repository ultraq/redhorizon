/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scripts

import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Circle
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

/**
 * Tests for the scripting system.
 *
 * @author Emanuel Rabina
 */
class ScriptSystemTests extends Specification {

	private static final SCREEN_BOUNDS = new Rectanglef(-400, -300, 400, 300)

	OpenGLWindow window
	OpenGLFramebuffer framebuffer
	BasicShader shader

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
			.centerToScreen()
			.scaleToFit()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
		framebuffer = new OpenGLFramebuffer(1600, 1200)
		shader = new BasicShader()
	}

	def cleanup() {
		shader?.close()
		framebuffer?.close()
		window?.close()
	}

	def 'Simulation timestep'() {
		given:
			var inputEventHandler = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)
			var scene = new Scene().tap {
				addChild(new Camera(800, 600, window::getViewport))
				addChild(new DebugOverlay()
					.withProfilingLogging())
				addChild(new Ball())
			}
			var engine = new Engine()
				.addSystem(new InputSystem(inputEventHandler))
				.addSystem(new ScriptSystem(new ScriptEngine('.'), inputEventHandler))
				.addSystem(new GraphicsSystem(window, framebuffer, shader))
				.withScene(scene)
		when:
			window.show()
			var deltaTimer = new DeltaTimer()
			while (!window.shouldClose()) {
				engine.update(deltaTimer.deltaTime())
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			scene?.close()
	}

	/**
	 * A moving object for the test so we can see how movement is affected by
	 * framerate and varying timestep solutions.
	 */
	static class Ball extends Node<Ball> {

		private static final float radius = 20f
		private static final float speed = 300f

		final Vector2f vector = new Vector2f()

		Ball() {

			addChild(new Circle(radius, Colour.GREEN, 32, true))
			addChild(new ScriptNode(BallScript))
		}

		static class BallScript extends Script<Ball> {

			private Scene scene

			// Flag to prevent executing the wall-bounce code multiple times because the
			// ball ends up over the edges for multiple frames
			private boolean bounced = false

			@Override
			void init() {

				scene = node.scene
			}

			@Override
			void update(float delta) {

				// Send the ball flying in some random direction
				if (input.keyPressed(GLFW_KEY_SPACE, true)) {
					node.vector.set(Math.random() * 2 - 1 as float, Math.random() * 2 - 1 as float).normalize()
				}

				var ballPosition = node.position

				// Reset the bounce flag once the ball is within the screen again
				if (bounced) {
					if ((ballPosition.x() - radius > SCREEN_BOUNDS.minX) &&
						(ballPosition.x() + radius < SCREEN_BOUNDS.maxX) &&
						(ballPosition.y() - radius > SCREEN_BOUNDS.minY) &&
						(ballPosition.y() + radius < SCREEN_BOUNDS.maxY)) {
						bounced = false
					}
				}

				// Check if the ball is going out the top/left/right of the screen
				if (!bounced) {
					if ((ballPosition.x() - radius < SCREEN_BOUNDS.minX) ||
						(ballPosition.x() + radius > SCREEN_BOUNDS.maxX)) {
						node.vector.x *= -1f
						bounced = true
					}
					if ((ballPosition.y() - radius < SCREEN_BOUNDS.minY) ||
						(ballPosition.y() + radius > SCREEN_BOUNDS.maxY)) {
						node.vector.y *= -1f
						bounced = true
					}
				}

				// Move the ball along its current trajectory
				if (node.vector) {
					node.translate(node.vector.x() * speed * delta as float, node.vector.y() * speed * delta as float)
				}
			}
		}
	}
}
