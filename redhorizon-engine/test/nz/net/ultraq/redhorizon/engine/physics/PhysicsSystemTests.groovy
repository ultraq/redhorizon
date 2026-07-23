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

package nz.net.ultraq.redhorizon.engine.physics

import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.engine.scripts.ScriptSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Circle
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.physics.BoxCollider
import nz.net.ultraq.redhorizon.physics.CollisionStartEvent
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector2f
import org.joml.primitives.Rectanglei
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

/**
 * Tests for the simulation system.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class PhysicsSystemTests extends Specification {

	private static final Rectanglei screen = new Rectanglei(-400, -300, 400, 300)

	def 'Simulation timestep - #frequency Hz'(int frequency) {
		given:
			var window = new OpenGLWindow(screen.lengthX(), screen.lengthY(), "Simulation timestep - ${frequency}Hz")
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.GREY)
				.withVSync(frequency as Boolean)
			var framebuffer = new OpenGLFramebuffer(screen.lengthX() * 2, screen.lengthY() * 2)
			var shader = new BasicShader()

			var inputEventHandler = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)
			var scene = new Scene()
				.addChild(new Camera(screen.lengthX(), screen.lengthY(), window::getViewport))
				.addChild(new DebugOverlay().withProfilingLogging())
				.addChild(new ScreenEdges())
				.addChild(new Ball())
			var engine = new Engine()
				.addSystem(new InputSystem(inputEventHandler))
				.addSystem(new ScriptSystem(new ScriptEngine('.'), inputEventHandler))
				.addSystem(new PhysicsSystem(frequency)
					.addSystem(new MovementSystem())
					.addSystem(new CollisionSystem())
				)
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
			shader?.close()
			framebuffer?.close()
			window?.close()
		where:
			frequency << [30, 60, 120, 0]
	}

	/**
	 * A moving object for the test so we can see how movement is affected by
	 * framerate and varying timestep solutions.
	 */
	static class Ball extends Node<Ball> {
		private static final float radius = 20f

		Ball() {
			addChild(new Circle(radius, Colour.GREEN, 32, true))
			addChild(new BoxCollider(radius * 2f as float, radius * 2f as float))
			addChild(new MovementNode(300f, randomVector(new Vector2f())))
			addChild(new ScriptNode(BallScript))
		}

		static randomVector(Vector2f dest) {
			return dest.set(Math.random() * 2 - 1 as float, Math.random() * 2 - 1 as float).normalize()
		}

		static class BallScript extends Script<Ball> {
			private MovementNode movement

			@Override
			void init() {
				movement = node.find(MovementNode)
				node.find(BoxCollider).on(CollisionStartEvent) { event ->
					var otherCollider = event.otherCollider()
					if (otherCollider.name == 'Top' || otherCollider.name == 'Bottom') {
						movement.vector.y *= -1f
					}
					else if (otherCollider.name == 'Left' || otherCollider.name == 'Right') {
						movement.vector.x *= -1f
					}
				}
			}

			@Override
			void update(float delta) {
				if (input.keyPressed(GLFW_KEY_SPACE, true)) {
					movement.vector.set(randomVector(new Vector2f()))
				}
			}
		}
	}

	/**
	 * Colliders created at the edges of the screen to keep the ball in play.
	 */
	class ScreenEdges extends Node<ScreenEdges> {

		ScreenEdges() {
			addChild(new BoxCollider(screen.lengthX(), 1).translate(0f, screen.maxY).withName('Top'))
			addChild(new BoxCollider(screen.lengthX(), 1).translate(0f, screen.minY).withName('Bottom'))
			addChild(new BoxCollider(1, screen.lengthY()).translate(screen.minX, 0f).withName('Left'))
			addChild(new BoxCollider(1, screen.lengthY()).translate(screen.maxX, 0f).withName('Right'))
		}
	}
}
