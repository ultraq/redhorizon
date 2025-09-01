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

package nz.net.ultraq.redhorizon.graphics.opengl

import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Material
import nz.net.ultraq.redhorizon.graphics.Window

import org.joml.Matrix4fc

import java.nio.ByteBuffer

/**
 * A built-in shader for rendering just the primitives provided by the graphics
 * module.
 *
 * @author Emanuel Rabina
 */
class BasicShader extends OpenGLShader {

	private final OpenGLTexture whiteTexture

	/**
	 * Constructor, create the built-in OpenGL shader.
	 */
	BasicShader() {

		super('Basic', 'nz/net/ultraq/redhorizon/graphics/opengl/Basic.glsl')
		whiteTexture = new OpenGLTexture(1, 1, 4, ByteBuffer.allocateNative(4).put(Colour.WHITE as byte[]).flip())
	}

	@Override
	void applyUniforms(Matrix4fc transform, Material material, Window window) {

		setUniform('model', transform)
		setUniform('mainTexture', 0, material.texture ?: whiteTexture)
	}

	@Override
	void close() {

		whiteTexture.close()
		super.close()
	}
}
