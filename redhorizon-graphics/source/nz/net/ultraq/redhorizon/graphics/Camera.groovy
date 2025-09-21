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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.eventhorizon.RemovalToken

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.primitives.Rectanglei
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL11.*

/**
 * The player's view into the world.
 *
 * @author Emanuel Rabina
 */
class Camera {

	private static final Logger logger = LoggerFactory.getLogger(Camera)

	private final Rectanglei viewport
	private boolean viewportChanged
	private RemovalToken windowResizeToken
	final Matrix4f projection
	final Matrix4f view
	private final Matrix4f viewProjection = new Matrix4f()

	/**
	 * Constructor, create a new 2D camera with the given view dimensions.
	 * The dimensions will serve as the expected aspect ratio of the viewport and
	 * retain it if tracking a window for resize events.
	 */
	Camera(int width, int height) {

		viewport = new Rectanglei(0, 0, width, height)
		projection = new Matrix4f().setOrthoSymmetric(width, height, 0, 10)
		view = new Matrix4f().setLookAt(
			0, 0, 10,
			0, 0, 0,
			0, 1, 0
		)
	}

	/**
	 * Attach the camera to a window for tracking resize events.
	 */
	Camera attachWindow(Window window) {

		// Can only track 1 window at a time
		// NOTE: At some point, we'll have cameras attached to framebuffers, so the
		//       method signature will need to change (RenderTarget?)
		if (windowResizeToken != null) {
			windowResizeToken.remove()
		}

		// Track window resizes and retain the aspect ratio while filling the available space
		windowResizeToken = new RemovalToken()
		var viewportWidth = window.framebufferWidth
		var viewportHeight = window.framebufferHeight
		window.on(FramebufferSizeEvent, windowResizeToken) { event ->
			var framebufferWidth = event.width()
			var framebufferHeight = event.height()
			var scale = Math.min(framebufferWidth / viewportWidth, framebufferHeight / viewportHeight)
			viewportWidth = (int)(viewportWidth * scale)
			viewportHeight = (int)(viewportHeight * scale)
			var viewportX = (framebufferWidth - viewportWidth) / 2 as int
			var viewportY = (framebufferHeight - viewportHeight) / 2 as int
			viewport.set(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight)
			logger.debug('Viewport updated: {}, {}, {}, {}', viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY())
			viewportChanged = true
		}

		return this
	}

	/**
	 * Convert a set of window coordinates to world coordinates.
	 *
	 * @param winX
	 * @param winY
	 * @param result A vector to store the result in.
	 */
	void unproject(float winX, float winY, Vector3f result) {

		projection.mulAffine(view, viewProjection).unproject(winX, winY, 0,
			new int[]{ viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY() },
			result)
	}

	/**
	 * Update rendering with the camera state.
	 */
	void update(Shader shader) {

		// TODO: Render context?
		if (viewportChanged) {
			glViewport(viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY())
			viewportChanged = false
		}
		shader.setUniform('projection', projection)
		shader.setUniform('view', view)
	}
}
