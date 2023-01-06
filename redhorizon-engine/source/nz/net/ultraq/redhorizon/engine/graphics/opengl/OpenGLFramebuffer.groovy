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

import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import static org.lwjgl.opengl.GL30C.*

/**
 * An OpenGL implementation of a framebuffer.
 *
 * @author Emanuel Rabina
 */
class OpenGLFramebuffer extends Framebuffer {

	final int framebufferId
	final int depthBufferId

	/**
	 * Constructor, create a new OpenGL framebuffer for rendering into.
	 *
	 * @param width
	 * @param height
	 * @param colourTexture
	 */
	OpenGLFramebuffer(int width, int height, OpenGLTexture colourTexture) {

		super(colourTexture)

		framebufferId = glGenFramebuffers()
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId)

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colourTexture.textureId, 0)

		// Depth buffer attachment
		depthBufferId = glGenRenderbuffers()
		glBindRenderbuffer(GL_RENDERBUFFER, depthBufferId)
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height)
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthBufferId)
	}

	@Override
	void bind() {

		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId)
	}

	@Override
	void close() {

		glDeleteFramebuffers(framebufferId)
		glDeleteRenderbuffers(depthBufferId)
		texture.close()
	}
}
