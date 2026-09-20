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
import nz.net.ultraq.redhorizon.scene.Scene
import nz.net.ultraq.redhorizon.time.DeltaTimer

import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.system.Configuration
import spock.lang.IgnoreIf
import spock.lang.Specification
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W
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
	Framebuffer framebuffer
	Basic3DShader shader
	GraphicsSystem graphicsSystem
	Scene scene
	DeltaTimer timer = new DeltaTimer()
	InputEventHandler input

	def setup() {
		window = new OpenGLWindow(800, 600, "3D model test")
			.centerToScreen()
			.scaleToFit()
			.withBackgroundColour(Colour.GREY)
			.withVSync(true)
		framebuffer = new OpenGLFramebuffer(1600, 1200)
		shader = new Basic3DShader()
		graphicsSystem = new GraphicsSystem(window, framebuffer, shader)
		scene = new Scene()
			.addChild(new DebugOverlay())
			.addChild(new Camera(8, 6))
		input = new InputEventHandler()
			.addInputSource(window)
			.addEscapeToCloseBinding(window)
			.addVSyncBinding(window)
	}

	def cleanup() {
		scene?.close()
		shader?.close()
		framebuffer?.close()
		window?.close()
	}

	def "Displays a 3D model"() {
		given:
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
			var material = new Material(
				ambientColour: new Vector4f(0.25f, 0.25f, 0.25f, 1f),
				lightColour: new Vector4f(0.8f, 0.8f, 1f, 1f),
				lightPosition: new Vector3f(5f, 5f, 5f)
			)
			var teapot = new Model(Type.TRIANGLES, vertices as Vertex[], index as int[], material)
			scene.addChild(teapot
				.translate(0f, -1.5f, 0f)
				.rotate(Math.toRadians(15) as float, 0f, 0f))
			var wireframeMode = true

		when:
			window.show()
			glEnable(GL_LINE_SMOOTH)
			while (!window.shouldClose()) {
				var delta = timer.deltaTime()
				input.processInputs()
				teapot.rotate(0f, 1f * delta as float, 0f)
				scene.find(Camera).unproject(window.viewport, input.cursorPosition(), material.lightPosition)
				glPolygonMode(GL_FRONT_AND_BACK, wireframeMode ? GL_LINE : GL_FILL)
				graphicsSystem.update(scene, delta)
				if (input.keyPressed(GLFW_KEY_W, true)) {
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

	/**
	 * A 3D model node.
	 */
	static class Model extends GraphicsNode<Model, SceneShaderContext> {

		final Class<? extends Shader> shaderClass = Basic3DShader
		final Mesh mesh
		final Material material

		Model(Type type, Vertex[] vertices, int[] index, Material material) {

			mesh = new OpenGLMesh(type, vertices, index)
			this.material = material
		}

		@Override
		void close() {

			mesh.close()
		}

		@Override
		void render(SceneShaderContext shaderContext) {

			mesh.render(shaderContext, material, transform)
		}
	}
}
