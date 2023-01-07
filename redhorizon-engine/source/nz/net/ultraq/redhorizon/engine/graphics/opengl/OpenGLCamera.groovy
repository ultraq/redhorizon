/* 
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Camera

import org.joml.Matrix4f
import static org.lwjgl.opengl.GL15C.*
import static org.lwjgl.opengl.GL30C.glBindBufferBase
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER
import static org.lwjgl.system.MemoryStack.stackPush

/**
 * A camera using OpenGL for its view into a scene.
 *
 * @author Emanuel Rabina
 */
class OpenGLCamera extends Camera {

	private final int cameraBufferObject

	/**
	 * Constructor, build a camera with the given rendering resolution.
	 *
	 * @param window
	 */
	OpenGLCamera(Dimension renderResolution) {

		super(renderResolution)

		cameraBufferObject = glGenBuffers()
		glBindBuffer(GL_UNIFORM_BUFFER, cameraBufferObject)

		def projectionAndViewBuffer = stackPush().withCloseable { stack -> stack.mallocFloat(Matrix4f.FLOATS * 2) }
		projection.get(0, projectionAndViewBuffer)
		view.get(Matrix4f.FLOATS, projectionAndViewBuffer)
		glBufferData(GL_UNIFORM_BUFFER, projectionAndViewBuffer, GL_DYNAMIC_DRAW)

		glBindBufferBase(GL_UNIFORM_BUFFER, 0, cameraBufferObject)
	}

	@Override
	void close() {

		glDeleteBuffers(cameraBufferObject)
	}

	@Override
	void update() {

		if (moved) {
			stackPush().withCloseable { stack ->
				glBindBuffer(GL_UNIFORM_BUFFER, cameraBufferObject)
				glBufferSubData(GL_UNIFORM_BUFFER, Matrix4f.BYTES, view.get(stack.mallocFloat(Matrix4f.FLOATS)))
			}
			moved = false
		}
	}
}
