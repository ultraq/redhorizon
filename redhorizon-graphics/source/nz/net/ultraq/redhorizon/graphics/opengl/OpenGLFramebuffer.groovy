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

package nz.net.ultraq.redhorizon.graphics.opengl

import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Mesh
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.PostProcessingShaderContext
import nz.net.ultraq.redhorizon.graphics.Texture
import nz.net.ultraq.redhorizon.graphics.Vertex

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Rectanglei
import static org.lwjgl.opengl.GL11C.*
import static org.lwjgl.opengl.GL20C.glUseProgram
import static org.lwjgl.opengl.GL30C.*

/**
 * An OpenGL implementation of a framebuffer.
 *
 * @author Emanuel Rabina
 */
class OpenGLFramebuffer implements Framebuffer {

	final int width
	final int height
	final Rectanglei viewport
	private final Mesh fullScreenQuad
	final Texture texture
	private final int framebufferId
	private final int depthBufferId

	/**
	 * Constructor, create a new OpenGL framebuffer for rendering into.
	 */
	OpenGLFramebuffer(int width, int height, boolean filter = false) {

		this.width = width
		this.height = height
		viewport = new Rectanglei(0, 0, width, height)

		fullScreenQuad = new OpenGLMesh(Type.TRIANGLES,
			new Vertex[]{
				new Vertex(new Vector3f(-1, -1, 0), Colour.WHITE, new Vector2f(0, 0)),
				new Vertex(new Vector3f(1, -1, 0), Colour.WHITE, new Vector2f(1, 0)),
				new Vertex(new Vector3f(1, 1, 0), Colour.WHITE, new Vector2f(1, 1)),
				new Vertex(new Vector3f(-1, 1, 0), Colour.WHITE, new Vector2f(0, 1))
			},
			new int[]{ 0, 1, 2, 2, 3, 0 }
		)

		framebufferId = glGenFramebuffers()
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId)

		texture = new OpenGLTexture(width, height, 4, filter)
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.textureId, 0)

		// Depth buffer attachment
		depthBufferId = glGenRenderbuffers()
		glBindRenderbuffer(GL_RENDERBUFFER, depthBufferId)
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height)
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthBufferId)
	}

	@Override
	void close() {

		glDeleteFramebuffers(framebufferId)
		glDeleteRenderbuffers(depthBufferId)
		texture.close()
		fullScreenQuad.close()
	}

	@Override
	void draw(PostProcessingShaderContext shaderContext) {

		fullScreenQuad.render(shaderContext, texture)
	}

	@Override
	OpenGLFramebuffer useFramebuffer(Closure closure) {

		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId)
		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LEQUAL)

		// Reset shader program when switching framebuffer.  Fixes an issue w/
		// nVidia on Windows where calling glClear() would then cause the following
		// error:
		// "Program/shader state performance warning: Vertex shader in program X
		// is being recompiled based on GL state"
		glUseProgram(0)

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
		glViewport(0, 0, width, height)
		closure()

		return this
	}
}
