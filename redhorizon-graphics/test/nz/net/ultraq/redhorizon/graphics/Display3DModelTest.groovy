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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.input.KeyEvent

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.system.Configuration
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.*

/**
 * A test for showing 3D models in the Red Horizon graphics module.
 *
 * <p>The model used is the classic <a href="https://graphics.cs.utah.edu/teapot/">Utah
 * Teapot</a>.
 *
 * @author Emanuel Rabina
 */
class Display3DModelTest extends Specification {

	def setupSpec() {
		Configuration.STACK_SIZE.set(10240)
	}

	OpenGLWindow window
	BasicShader shader
	Framebuffer framebuffer

	def setup() {
		window = new OpenGLWindow(800, 600, "3D model test")
			.centerToScreen()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
			.on(KeyEvent) { event ->
				if (event.keyPressed(GLFW_KEY_ESCAPE)) {
					window.shouldClose(true)
				}
			}
		shader = new BasicShader()
		framebuffer = new OpenGLFramebuffer(800, 600)
	}

	def cleanup() {
		shader?.close()
		framebuffer?.close()
		window?.close()
	}

	def "Displays a 3D model"() {
		given:
			var camera = new Camera(8, 6)
			var debugOverlay = new DebugOverlay()
			var input = new InputEventHandler()
				.addInputSource(window)
			var vertices = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/Display3DModelTest_UtahTeapot.obj').withBufferedStream { stream ->
				return new ObjFileReader().read(stream)
			}
			var teapot = new OpenGLMesh(Type.POINTS, vertices.collect { it ->
				var result = new Vertex(it, Colour.WHITE)
				return result
			} as Vertex[])
			var transform = new Matrix4f()
				.translate(0f, -1.5f, 0f)
				.rotateX(Math.toRadians(15) as float)

		when:
			window.show()
			while (!window.shouldClose()) {
				transform.rotateY(0.01f)
				window.useRenderPipeline()
					.scene { ->
						framebuffer.useFramebuffer { ->
							shader.useShader { shaderContext ->
								camera.render(shaderContext)
								teapot.render(shaderContext, null, transform)
							}
						}
					}
					.ui(false) { imGuiContext ->
						debugOverlay.render(imGuiContext)
					}
					.end()
				if (input.keyPressed(GLFW_KEY_V, true)) {
					window.toggleVSync()
				}
				Thread.yield()
			}

		then:
			noExceptionThrown()
		cleanup:
			teapot?.close()
	}

	/**
	 * A simple Waveform OBJ file reader, interested in only the vertices.
	 */
	static class ObjFileReader {

		List<Vector3f> read(InputStream input) {

			var vertices = new ArrayList<Vector3f>()
			input.readLines().each { line ->
				if (line.startsWith('v ')) {
					var parts = line.split(' ')
					vertices << new Vector3f(parts[1] as float, parts[2] as float, parts[3] as float)
				}
			}
			return vertices
		}
	}
}
