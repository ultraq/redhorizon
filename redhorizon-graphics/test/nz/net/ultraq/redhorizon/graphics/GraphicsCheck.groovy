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
import org.joml.Vector3f
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

import java.nio.ByteBuffer

/**
 * A simple test for making sure we can create a window.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class GraphicsCheck extends Specification {

	def setupSpec() {
		if (System.isMacOs()) {
			System.setProperty('org.lwjgl.glfw.libname', 'glfw_async')
		}
	}

	def "Opens a window"() {
		when:
			var window = new OpenGLWindow(800, 600, "Testing")
				.withBackgroundColour(Colour.WHITE)
				.show()
			window.on(KeyEvent) { event ->
				if (event.isKeyPress(GLFW_KEY_ESCAPE)) {
					window.shouldClose(true)
				}
			}
			while (!window.shouldClose()) {
				window.withFrame { ->
					// Do something!
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			window?.close()
	}

	def "Draws a triangle"() {
		when:
			var window = new OpenGLWindow(800, 600, "Testing")
				.withBackgroundColour(Colour.WHITE)
				.show()
			window.on(KeyEvent) { event ->
				if (event.isKeyPress(GLFW_KEY_ESCAPE)) {
					window.shouldClose(true)
				}
			}
			var whiteTexture = new OpenGLTexture(1, 1, 4, ByteBuffer.allocateNative(4).put(Colour.WHITE as byte[]).flip())
			var shader = new BasicShader(whiteTexture)
			var triangle = new OpenGLMesh(Type.TRIANGLES, new Vertex[]{
				new Vertex(new Vector3f(0.0, 0.5, 0.0), Colour.RED),
				new Vertex(new Vector3f(-0.5, -0.5, 0.0), Colour.RED),
				new Vertex(new Vector3f(0.5, -0.5, 0.0), Colour.RED)
			})
			var transform = new Matrix4f()
			var material = new Material()
			while (!window.shouldClose()) {
				window.withFrame { ->
					shader.use()
					shader.applyUniforms(transform, material, window)
					triangle.draw()
				}
				Thread.yield()
			}
		then:
			notThrown(Exception)
		cleanup:
			triangle?.close()
			shader?.close()
			whiteTexture?.close()
			window?.close()
	}
}
