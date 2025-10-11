/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics.opengl

import nz.net.ultraq.redhorizon.graphics.LibRetroShaderReader
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.ShaderContext
import nz.net.ultraq.redhorizon.graphics.Texture
import nz.net.ultraq.redhorizon.graphics.Vertex

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector4f
import org.joml.Vector4fc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL11C.GL_TRUE
import static org.lwjgl.opengl.GL20C.*
import static org.lwjgl.system.MemoryStack.stackPush

import groovy.transform.Memoized
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import java.nio.Buffer

/**
 * OpenGL-specific shader implementation.
 *
 * @author Emanuel Rabina
 */
abstract class OpenGLShader<TShaderContext extends ShaderContext> implements Shader<TShaderContext> {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLShader)

	final String name
	final int programId
	protected final Map<String, Buffer> uniformBuffers = [:]
	@Lazy
	protected TShaderContext renderContext = { createShaderContext() }()

	/**
	 * Constructor, build an OpenGL shader program from the vertex and fragment
	 * shader source.
	 */
	OpenGLShader(String name, String shaderSourcePath) {

		this.name = name

		def (vertexShaderSource, fragmentShaderSource) = new LibRetroShaderReader().read(shaderSourcePath)

		/*
		 * Create a shader of the specified name and type, running a compilation
		 * check to make sure it all went OK.
		 */
		var compileShader = { String shaderSource, int type ->
			var shaderId = glCreateShader(type)
			glShaderSource(shaderId, shaderSource)
			glCompileShader(shaderId)

			var status = glGetShaderi(shaderId, GL_COMPILE_STATUS)
			if (status != GL_TRUE) {
				var message = glGetShaderInfoLog(shaderId)
				logger.error(message)
				throw new Exception(message)
			}

			return shaderId
		}

		/*
		 * Link multiple shader parts together into a shader program.
		 */
		var linkShaderProgram = { int vertexShaderId, int fragmentShaderId ->
			var programId = glCreateProgram()
			glAttachShader(programId, vertexShaderId)
			glAttachShader(programId, fragmentShaderId)

			// Control binding points for attributes in our shaders
			Vertex.LAYOUT.each { attribute ->
				glBindAttribLocation(programId, attribute.location(), attribute.name())
			}

			glLinkProgram(programId)
			glValidateProgram(programId)

			var status = glGetProgrami(programId, GL_LINK_STATUS)
			if (status != GL_TRUE) {
				var message = glGetProgramInfoLog(programId)
				logger.error(message)
				throw new Exception(message)
			}

			return programId
		}

		var vertexShaderId = compileShader(vertexShaderSource, GL_VERTEX_SHADER)
		var fragmentShaderId = compileShader(fragmentShaderSource, GL_FRAGMENT_SHADER)

		programId = linkShaderProgram(vertexShaderId, fragmentShaderId)

		glDeleteShader(vertexShaderId)
		glDeleteShader(fragmentShaderId)
	}

	@Override
	void close() {

		glDeleteProgram(programId)
	}

	/**
	 * Create the render context to use for rendering with this shader.
	 */
	protected abstract TShaderContext createShaderContext()

	/**
	 * Cached function for looking up a uniform location in a shader program.
	 */
	@Memoized
	protected int getUniformLocation(String name) {

		return glGetUniformLocation(programId, name)
	}

	@Override
	void setUniform(String name, float value) {

		glUniform1f(getUniformLocation(name), value)
	}

	@Override
	void setUniform(String name, float[] values) {

		var uniformLocation = getUniformLocation(name)
		stackPush().withCloseable { stack ->
			var buffer = uniformBuffers
				.getOrCreate(name) { -> stack.mallocFloat(values.length) }
				.put(values)
				.flip()
			switch (values.length) {
				case 2 -> glUniform2fv(uniformLocation, buffer)
				default -> glUniform1fv(uniformLocation, buffer)
			}
		}
	}

	@Override
	void setUniform(String name, int value) {

		glUniform1i(getUniformLocation(name), value)
	}

	@Override
	void setUniform(String name, int[] values) {

		glUniform1iv(getUniformLocation(name), values)
	}

	@Override
	void setUniform(String name, Matrix4fc value) {

		stackPush().withCloseable { stack ->
			var buffer = uniformBuffers.getOrCreate(name) { -> stack.mallocFloat(Matrix4f.FLOATS) }
			value.get(buffer)
			glUniformMatrix4fv(getUniformLocation(name), false, buffer)
		}
	}

	@Override
	void setUniform(String name, Vector2fc value) {

		var uniformLocation = getUniformLocation(name)
		stackPush().withCloseable { stack ->
			var buffer = uniformBuffers
				.getOrCreate(name) { -> stack.mallocFloat(Vector2f.FLOATS) }
				.put(value.x(), value.y())
				.flip()
			glUniform2fv(uniformLocation, buffer)
		}
	}

	@Override
	void setUniform(String name, Vector4fc[] values) {

		var uniformLocation = getUniformLocation(name)
		stackPush().withCloseable { stack ->
			var buffer = uniformBuffers.getOrCreate(name) { -> stack.mallocFloat(values.length * Vector4f.FLOATS) }
			values.each { value ->
				buffer.put(value.x(), value.y(), value.z(), value.w())
			}
			buffer.flip()
			glUniform4fv(uniformLocation, buffer)
		}
	}

	@Override
	void setUniform(String name, int textureUnit, Texture texture) {

		glUniform1i(getUniformLocation(name), textureUnit)
		texture.bind(textureUnit)
	}

	@Override
	void useShader(@ClosureParams(value = FromString, options = 'TShaderContext') Closure closure) {

		glUseProgram(programId)
		closure(renderContext)
	}
}
