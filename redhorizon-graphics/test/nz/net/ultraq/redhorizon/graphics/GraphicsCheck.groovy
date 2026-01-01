/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.system.Configuration
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

/**
 * A simple test for making sure we can render objects using the graphics
 * module.
 *
 * <p>The image file used for testing is {@code ship_0000.png} from
 * <a href="https://kenney.nl/assets/pixel-shmup">Kenney's Pixel Shmup</a>.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class GraphicsCheck extends Specification {

	def setupSpec() {
		Configuration.STACK_SIZE.set(10240)
		if (System.isMacOs()) {
			Configuration.GLFW_LIBRARY_NAME.set('glfw_async')
		}
	}

	OpenGLWindow window
	Matrix4f cameraTransform = new Matrix4f()

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
			.centerToScreen()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
			.on(KeyEvent) { event ->
				if (event.keyPressed(GLFW_KEY_ESCAPE)) {
					window.shouldClose(true)
				}
			}
	}

	def cleanup() {
		window?.close()
	}

	def "Opens a window"() {
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					// Do something!
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
	}

	def "Opens a window with the debug overlay"() {
		given:
			var emptyFramebuffer = new OpenGLFramebuffer(800, 600)
			var debugOverlay = new DebugOverlay()
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useRenderPipeline()
					.scene { ->
						return emptyFramebuffer
					}
					.ui(false) { imGuiContext ->
						debugOverlay.render(imGuiContext)
					}
					.end()
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			emptyFramebuffer?.close()
	}

	def "Draws a triangle"() {
		given:
			var shader = new BasicShader()
			var triangle = new OpenGLMesh(Type.TRIANGLES, new Vertex[]{
				new Vertex(new Vector3f(0, 3, 0), Colour.RED),
				new Vertex(new Vector3f(-3, -3, 0), Colour.GREEN),
				new Vertex(new Vector3f(3, -3, 0), Colour.BLUE)
			})
			var triangleTransform = new Matrix4f()
			var camera = new Camera(10, 10, window)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.render(shaderContext, cameraTransform)
						triangle.render(shaderContext, null, triangleTransform)
					}
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			triangle?.close()
			shader?.close()
	}

	def "Draws a sprite - using Image and ImageDecoder SPI"() {
		given:
			var shader = new BasicShader()
			var image = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/GraphicsCheck_Texture_ship0000.png').withBufferedStream { stream ->
				return new Image('GraphicsCheck_Texture_ship0000.png', stream)
			}
			var sprite = new Sprite(image)
			var spriteTransform = new Matrix4f()
			var camera = new Camera(80, 60, window)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.useWindow { ->
					shader.useShader { shaderContext ->
						camera.render(shaderContext, cameraTransform)
						sprite.render(shaderContext, spriteTransform)
					}
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			sprite?.close()
			image?.close()
			shader?.close()
	}
}
