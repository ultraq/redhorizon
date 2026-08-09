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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.GraphicsNode
import nz.net.ultraq.redhorizon.graphics.Rectangle
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * Tests for the node properties panel and its ability to modify values on the
 * fly.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class NodePropertiesTests extends Specification {

	OpenGLWindow window
	OpenGLFramebuffer framebuffer
	BasicShader shader

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
			.centerToScreen()
			.scaleToFit()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
		framebuffer = new OpenGLFramebuffer(800, 600)
		shader = new BasicShader()
	}

	def cleanup() {
		shader?.close()
		framebuffer?.close()
		window?.close()
	}

	def "Can modify a node's values"() {
		var testNode = new TestNode()
		var scene = new Scene()
			.addChild(new Camera(800, 600))
			.addChild(testNode)
			.addChild(new NodeList())
			.addChild(new NodeProperties())
		var input = new InputEventHandler()
			.addInputSource(window)
			.addEscapeToCloseBinding(window)
		when:
			window.show()
			while (!window.shouldClose()) {
				input.processInputs()
				testNode.update()
				window.useRenderPipeline()
					.scene { ->
						return framebuffer.useFramebuffer { ->
							shader.useShader { shaderContext ->
								scene.find(Camera).render(shaderContext)
								scene.findAll(GraphicsNode)*.render(shaderContext)
							}
						}
					}
					.ui(true) { imGuiContext ->
						scene.findAll(ImGuiModule)*.render(imGuiContext)
					}
					.end()
				Thread.yield()
			}
		then:
			noExceptionThrown()
	}

	/**
	 * A node with various properties to see if they can be modified.
	 */
	static class TestNode extends Node<TestNode> {

		private final Rectangle square
		public float x = 0f // Has public modifier, so should appear as a public field
		float y = 0f // No modifier, so Groovy will generate get/set methods that we need to extract

		TestNode() {

			square = new Rectangle(20f, 20f, Colour.RED, true)
			addChild(square)
		}

		void update() {

			setPosition(x, y)
		}
	}
}
