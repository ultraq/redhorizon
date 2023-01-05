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

import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.Uniform

import org.joml.Matrix4f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL11C.GL_TRUE
import static org.lwjgl.opengl.GL20C.*
import static org.lwjgl.opengl.GL31C.glGetUniformBlockIndex
import static org.lwjgl.opengl.GL31C.glUniformBlockBinding
import static org.lwjgl.system.MemoryStack.stackPush

import groovy.transform.Memoized

/**
 * OpenGL-specific shader implementation.
 *
 * @author Emanuel Rabina
 */
class OpenGLShader extends Shader {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLShader)

	private final int programId

	/**
	 * Constructor, build an OpenGL shader program from the vertex and fragment
	 * shaders.
	 *
	 * @param name
	 * @param vertexShaderSource
	 * @param fragmentShaderSource
	 * @param uniforms
	 */
	OpenGLShader(String name, String vertexShaderSource, String fragmentShaderSource, Uniform... uniforms) {

		super(name, uniforms)

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

		// Tie the shader's view/projection uniforms to the camera
		def blockIndex = glGetUniformBlockIndex(programId, 'Camera')
		glUniformBlockBinding(programId, blockIndex, 0)
	}

	@Override
	void close() {

		glDeleteProgram(programId)
	}

	/**
	 * Cached function for looking up a uniform location in a shader program.
	 *
	 * @param name
	 * @return
	 */
	@Memoized
	private int getUniformLocation(String name) {

		return glGetUniformLocation(programId, name)
	}

	@Override
	void setUniform(String name, float[] data) {

		stackPush().withCloseable { stack ->
			switch (data.length) {
				case 2:
					glUniform2fv(getUniformLocation(name), stack.floats(data))
					break
				default:
					throw new UnsupportedOperationException("Uniform data of size ${data.length} is not supported")
			}
		}
	}

	@Override
	void setUniform(String name, int[] data) {

		stackPush().withCloseable { stack ->
			glUniform1iv(getUniformLocation(name), stack.ints(data))
		}
	}

	@Override
	void setUniformMatrix(String name, Matrix4f matrix) {

		stackPush().withCloseable { stack ->
			glUniformMatrix4fv(getUniformLocation(name), false, matrix.get(stack.mallocFloat(16)))
		}
	}

	@Override
	void setUniformTexture(String name, int textureUnit, Texture texture) {

		glUniform1i(getUniformLocation(name), textureUnit)
		texture.bind(textureUnit)
	}

	@Override
	void use() {

		glUseProgram(programId)
	}
}
