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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.graphics.Attribute
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.Uniform

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL11C.GL_TRUE
import static org.lwjgl.opengl.GL20C.*
import static org.lwjgl.system.MemoryStack.stackPush

import groovy.transform.Memoized
import groovy.transform.PackageScope
import java.nio.Buffer

/**
 * OpenGL-specific shader implementation.
 *
 * @author Emanuel Rabina
 */
class OpenGLShader extends Shader {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLShader)
	private static int lastProgramId = 0

	final int programId
	private final Map<String, Buffer> uniformBuffers = [:]

	/**
	 * Constructor, build an OpenGL shader program from the vertex and fragment
	 * shaders.
	 */
	OpenGLShader(String name, String vertexShaderSource, String fragmentShaderSource, Attribute[] attributes, Uniform[] uniforms) {

		super(name, attributes, uniforms)

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
			attributes.each { attribute ->
				glBindAttribLocation(programId, attribute.location, attribute.name)
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
	 * Cached function for looking up a uniform location in a shader program.
	 */
	@Memoized
	private int getUniformLocation(String name) {

		return glGetUniformLocation(programId, name)
	}

	@Override
	void setUniform(String name, Object data) {

		var uniformLocation = getUniformLocation(name)

		stackPush().withCloseable { stack ->
			switch (data) {
				case Float -> glUniform1f(uniformLocation, data)
				case float[] -> {
					var floatData = (float[])data
					var buffer = uniformBuffers.getOrCreate(name) { -> stack.mallocFloat(floatData.length) }
					buffer.put(0, floatData)
					switch (floatData.length) {
						case 2 -> glUniform2fv(uniformLocation, buffer)
						default -> glUniform1fv(uniformLocation, buffer)
					}
				}
				case Integer -> glUniform1i(uniformLocation, data)
				case int[] -> glUniform1iv(uniformLocation, (int[])data)
				case Matrix4fc -> {
					var buffer = uniformBuffers.getOrCreate(name) { -> stack.mallocFloat(Matrix4f.FLOATS) }
					data.get(buffer)
					glUniformMatrix4fv(uniformLocation, false, buffer)
				}
				default -> throw new UnsupportedOperationException("Data type of ${data.class.simpleName} not supported")
			}
		}
	}

	@Override
	void setUniformTexture(String name, int textureUnit, Texture texture) {

		glUniform1i(getUniformLocation(name), textureUnit)
		texture.bind(textureUnit)
	}

	@Override
	void use() {

		useProgram(programId)
	}

	/**
	 * Set to use the OpenGL shader program with the given ID.
	 * <p>
	 * This is only used by the {@link OpenGLRenderer} to reset the program used
	 * so that program state doesn't bleed into the next one.
	 */
	@PackageScope
	static void useProgram(int programId) {

		if (programId != lastProgramId) {
			glUseProgram(programId)
			lastProgramId = programId
		}
	}
}
