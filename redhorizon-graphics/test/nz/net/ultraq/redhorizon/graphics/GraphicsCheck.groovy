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
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.KeyEvent

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.*

import java.nio.ByteBuffer
import javax.imageio.ImageIO

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
		System.setProperty('org.lwjgl.system.stackSize', '10240')
		if (System.isMacOs()) {
			System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
		}
	}

	OpenGLWindow window

	def setup() {
		window = new OpenGLWindow(800, 600, "Testing")
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
				window.withFrame { ->
					// Do something!
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
	}

	def "Draws a triangle"() {
		given:
			var shader = new BasicShader()
			var triangle = new OpenGLMesh(Type.TRIANGLES, new Vertex[]{
				new Vertex(new Vector3f(0, 3, 0), Colour.RED),
				new Vertex(new Vector3f(-3, -3, 0), Colour.GREEN),
				new Vertex(new Vector3f(3, -3, 0), Colour.BLUE)
			})
			var camera = new Camera(10, 10)
			var transform = new Matrix4f()
		when:
			window.show()
			while (!window.shouldClose()) {
				window.withFrame { ->
					var renderContext = shader.use()
					camera.update(renderContext)
					renderContext.setModelMatrix(transform)
					triangle.draw()
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			triangle?.close()
			shader?.close()
	}

	def "Draws a texture"() {
		given:
			var shader = new BasicShader()
			var quad = new OpenGLMesh(Type.TRIANGLES,
				new Vertex[]{
					new Vertex(new Vector3f(-2, -2, 0.0), Colour.WHITE, new Vector2f(0, 0)),
					new Vertex(new Vector3f(2, -2, 0.0), Colour.WHITE, new Vector2f(1, 0)),
					new Vertex(new Vector3f(2, 2, 0.0), Colour.WHITE, new Vector2f(1, 1)),
					new Vertex(new Vector3f(-2, 2, 0.0), Colour.WHITE, new Vector2f(0, 1))
				},
				new int[]{ 0, 1, 2, 2, 3, 0 })
			var imageStream = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/GraphicsCheck.png')
			var bufferedImage = ImageIO.read(imageStream)
			var width = bufferedImage.width
			var height = bufferedImage.height
			var channels = bufferedImage.colorModel.numComponents
			var texture = new OpenGLTexture(width, height, channels,
				bufferedImage.getRGB(0, 0, width, height, null, 0, width)
					.inject(ByteBuffer.allocateNative(width * height * channels)) { ByteBuffer acc, pixel ->
						var red = (byte)(pixel >> 16)
						var green = (byte)(pixel >> 8)
						var blue = (byte)(pixel)
						var alpha = (byte)(pixel >> 24)
						acc.put(red).put(green).put(blue).put(alpha)
					}
					.flip()
					.flipVertical(width, height, channels))
			var camera = new Camera(8, 6)
			var transform = new Matrix4f()
			var material = new Material(texture: texture)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.withFrame { ->
					var renderContext = shader.use()
					camera.update(renderContext)
					renderContext.setModelMatrix(transform)
					renderContext.setMaterial(material)
					quad.draw()
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			texture?.close()
			imageStream?.close()
			quad?.close()
			shader?.close()
	}

	def "Draws a sprite - using Image and ImageDecoder SPI"() {
		given:
			var shader = new BasicShader()
			var imageStream = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/GraphicsCheck.png')
			var image = new Image('GraphicsCheck.png', imageStream)
			var sprite = new Sprite(image)
			var camera = new Camera(80, 60)
				.attachWindow(window)
			camera.view.translate(-16, -16, 0)
		when:
			window.show()
			while (!window.shouldClose()) {
				window.withFrame { ->
					var renderContext = shader.use()
					camera.update(renderContext)
					sprite.draw(renderContext)
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			sprite?.close()
			image?.close()
			imageStream?.close()
			shader?.close()
	}
}
