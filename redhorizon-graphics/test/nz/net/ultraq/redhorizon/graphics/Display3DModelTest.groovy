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
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.input.KeyEvent

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.system.Configuration
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.*
import static org.lwjgl.opengl.GL11C.*

import groovy.transform.TupleConstructor

/**
 * A test for showing 3D models in the Red Horizon graphics module.
 *
 * <p>The model used is the classic <a href="https://graphics.cs.utah.edu/teapot/">Utah
 * Teapot</a>.
 *
 * @author Emanuel Rabina
 */
@IgnoreIf({ env.CI })
class Display3DModelTest extends Specification {

	def setupSpec() {
		Configuration.STACK_SIZE.set(10240)
	}

	OpenGLWindow window
	Basic3DShader shader
	Framebuffer framebuffer

	def setup() {
		window = new OpenGLWindow(800, 600, "3D model test")
			.centerToScreen()
			.scaleToFit()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
			.on(KeyEvent) { event ->
				if (event.keyPressed(GLFW_KEY_ESCAPE)) {
					window.shouldClose(true)
				}
			}
		shader = new Basic3DShader()
		framebuffer = new OpenGLFramebuffer(1600, 1200)
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
			var object = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/Display3DModelTest_UtahTeapot.obj').withBufferedStream { stream ->
				return new ObjFileReader().read(stream)
			}
			var vertices = new ArrayList<Vertex>()
			object.vertices.eachWithIndex { vertex, i ->
				vertices << new Vertex(vertex, Colour.WHITE, new Vector2f(), object.normals.get(i))
			}
			var index = new ArrayList<Integer>()
			object.faces.each { face ->
				index.addAll(face.vertexIndex)
			}
			var teapot = new OpenGLMesh(Type.TRIANGLES, vertices as Vertex[], index as int[])
			var material = new Material(
				ambientColour: new Vector4f(0.25f, 0.25f, 0.25f, 1f),
				lightColour: new Vector4f(0.8f, 0.8f, 1f, 1f),
				lightPosition: new Vector3f(5f, 5f, 5f)
			)
			var transform = new Matrix4f()
				.translate(0f, -1.5f, 0f)
				.rotateX(Math.toRadians(15) as float)
			var wireframeMode = true

		when:
			var lastTimeMillis = System.currentTimeMillis()
			window.show()
			glEnable(GL_LINE_SMOOTH)
			while (!window.shouldClose()) {
				var now = System.currentTimeMillis()
				var delta = (now - lastTimeMillis) / 1000 as float
				lastTimeMillis = now
				transform.rotateY(1f * delta as float)
				camera.unproject(window.viewport, input.cursorPosition(), material.lightPosition)
				window.useRenderPipeline()
					.scene { ->
						framebuffer.useFramebuffer { ->
							glPolygonMode(GL_FRONT_AND_BACK, wireframeMode ? GL_LINE : GL_FILL)
							shader.useShader { shaderContext ->
								camera.render(shaderContext)
								teapot.render(shaderContext, material, transform)
							}
							glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
						}
					}
					.ui(false) { imGuiContext ->
						debugOverlay.render(imGuiContext)
					}
					.end()
				if (input.keyPressed(GLFW_KEY_V, true)) {
					window.toggleVSync()
				}
				else if (input.keyPressed(GLFW_KEY_W, true)) {
					wireframeMode = !wireframeMode
				}
				Thread.yield()
			}

		then:
			noExceptionThrown()
		cleanup:
			teapot?.close()
	}

	@TupleConstructor(defaults = false)
	static class ObjFile {

		final List<Vector3f> vertices
		final List<Vector3f> normals
		final List<ObjectFace> faces
	}

	@TupleConstructor(defaults = false)
	static class ObjectFace {

		final List<Integer> vertexIndex
		final List<Integer> normalIndex
	}

	/**
	 * A simple Waveform OBJ file reader, interested in only the vertices.
	 */
	static class ObjFileReader {

		ObjFile read(InputStream input) {

			var vertices = new ArrayList<Vector3f>()
			var normals = new ArrayList<Vector3f>()
			var faces = new ArrayList<ObjectFace>()
			input.readLines().each { line ->
				if (line.startsWith('v ')) {
					var parts = line.split(' ')
					vertices << new Vector3f(parts[1] as float, parts[2] as float, parts[3] as float)
				}
				else if (line.startsWith('vn ')) {
					var parts = line.split(' ')
					normals << new Vector3f(parts[1] as float, parts[2] as float, parts[3] as float)
				}
				else if (line.startsWith('f ')) {
					var faceVertices = []
					var faceNormals = []
					line.split(' ').tail().each { part ->
						var parts = part.split('/')
						faceVertices << (parts[0] as int) - 1
						faceNormals << (parts[2] as int) - 1
					}
					faces << new ObjectFace(faceVertices, faceNormals)
				}
			}

			return new ObjFile(vertices, normals, faces)
		}
	}

	/**
	 * A shader made for displaying 3D objects.
	 */
	static class Basic3DShader extends OpenGLShader<SceneShaderContext> {

		Basic3DShader() {
			super('Basic3D', 'nz/net/ultraq/redhorizon/graphics/Display3DModelTest_Shader.glsl')
		}

		@Override
		protected SceneShaderContext createShaderContext() {

			return new SceneShaderContext() {

				@Override
				void setMaterial(Material material) {
					setUniform('ambientColour', material?.ambientColour ?: new Vector4f())
					setUniform('lightColour', material?.lightColour ?: new Vector4f())
					setUniform('lightPosition', material?.lightPosition ?: new Vector3f())
				}

				@Override
				void setModelMatrix(Matrix4fc model) {
					setUniform('model', model)
				}

				@Override
				void setProjectionMatrix(Matrix4fc projection) {
					setUniform('projection', projection)
				}

				@Override
				void setViewMatrix(Matrix4fc view) {
					setUniform('view', view)
				}
			}
		}
	}
}
