/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.graphics.UniformBuffer

import static org.lwjgl.opengl.GL15C.*
import static org.lwjgl.opengl.GL30C.glBindBufferBase
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER
import static org.lwjgl.system.MemoryStack.stackPush

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.concurrent.Semaphore

/**
 * An OpenGL implementation of the shared uniform object.
 *
 * @author Emanuel Rabina
 */
class OpenGLUniformBuffer extends UniformBuffer {

	private static final Semaphore uniformBlockIndexSemaphore = new Semaphore(1, true)
	private static final Map<String, Integer> uniformBlockIndices = [:]
	private static int uniformBlockCounter = 0

	final int blockIndex
	private final int bufferId

	/**
	 * Constructor, create a new uniform buffer object to hold this initial data.
	 */
	OpenGLUniformBuffer(String name, Buffer initialData, boolean global) {

		super(name)

		bufferId = glGenBuffers()
		glBindBuffer(GL_UNIFORM_BUFFER, bufferId)
		stackPush().withCloseable { stack ->
			switch (initialData) {
				case ByteBuffer -> {
					// TODO: Param so we can specify GL_STATIC_DRAW
					glBufferData(GL_UNIFORM_BUFFER, stack.malloc(initialData.capacity()).put(initialData).flip(), GL_DYNAMIC_DRAW)
				}
				case FloatBuffer -> {
					glBufferData(GL_UNIFORM_BUFFER, stack.mallocFloat(initialData.capacity()).put(initialData).flip(), GL_DYNAMIC_DRAW)
				}
				case IntBuffer -> {
					glBufferData(GL_UNIFORM_BUFFER, stack.mallocInt(initialData.capacity()).put(initialData).flip(), GL_DYNAMIC_DRAW)
				}
				default -> throw new UnsupportedOperationException("Buffer of type ${initialData.class} not supported as input to a uniform buffer object")
			}
		}
		initialData.rewind()
		blockIndex = uniformBlockIndexSemaphore.acquireAndRelease { ->
			return uniformBlockIndices.getOrCreate(name) { ->
				return uniformBlockCounter++
			}
		}

		if (global) {
			bind()
		}
	}

	/**
	 * A buffer is considered truthy/ready if the bufferId is valid.
	 */
	boolean asBoolean() {

		return glIsBuffer(bufferId)
	}

	@Override
	void bind() {

		glBindBufferBase(GL_UNIFORM_BUFFER, blockIndex, bufferId)
	}

	@Override
	void close() {

		glDeleteBuffers(bufferId)
	}

	@Override
	void updateBufferData(Buffer data, int offset = 0) {

		glBindBuffer(GL_UNIFORM_BUFFER, bufferId)
		stackPush().withCloseable { stack ->
			switch (data) {
				case ByteBuffer -> {
					glBufferSubData(GL_UNIFORM_BUFFER, offset, stack.malloc(data.capacity()).put(data).flip())
				}
				case FloatBuffer -> {
					glBufferSubData(GL_UNIFORM_BUFFER, offset, stack.mallocFloat(data.capacity()).put(data).flip())
				}
				default -> throw new UnsupportedOperationException("Buffer of type ${data.class} not supported as input to a uniform buffer object")
			}
		}
		data.rewind()
	}
}
