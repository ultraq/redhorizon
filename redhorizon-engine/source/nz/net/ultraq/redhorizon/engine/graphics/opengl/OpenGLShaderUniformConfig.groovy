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
import nz.net.ultraq.redhorizon.engine.graphics.ShaderUniformConfig

import org.joml.Matrix4f
import static org.lwjgl.opengl.GL41C.*
import static org.lwjgl.system.MemoryStack.stackPush

import groovy.transform.Memoized
import groovy.transform.TupleConstructor

/**
 * OpenGL-specific class for applying uniforms to shaders.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class OpenGLShaderUniformConfig implements ShaderUniformConfig {

	final Shader shader

	/**
	 * Cached function for looking up a uniform location in a shader program.
	 * 
	 * @param name
	 * @return
	 */
	@Memoized
	private int getUniformLocation(String name) {

		return glGetUniformLocation(shader.programId, name)
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
	void setUniformTexture(String name, int textureUnit, int textureId) {

		glUniform1i(getUniformLocation(name), textureUnit)
		glActiveTexture(GL_TEXTURE0 + textureUnit)
		glBindTexture(GL_TEXTURE_2D, textureId)
	}
}
