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

import nz.net.ultraq.redhorizon.scenegraph.Node

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f

/**
 * The player's view into the world.
 *
 * @author Emanuel Rabina
 */
class Camera extends Node<Camera> {

	private final Window window
	private final Matrix4f projection
	private final Matrix4f view
	private final Matrix4f viewProjection = new Matrix4f()
	private final Matrix4f viewTransform = new Matrix4f()

	/**
	 * Constructor, create a new 2D camera with the given view dimensions.
	 * The dimensions will serve as the expected aspect ratio of the viewport and
	 * retain it if tracking a window for resize events.
	 */
	Camera(int width, int height, Window window) {

		this.window = window
		projection = new Matrix4f().setOrthoSymmetric(width, height, 0, 10)
		view = new Matrix4f().setLookAt(
			0, 0, 10,
			0, 0, 0,
			0, 1, 0
		)
	}

	/**
	 * Return a view-projection matrix for the given transform.
	 */
	private Matrix4fc getViewProjection() {

		return projection.mulAffine(view.mul(transform, viewTransform), viewProjection)
	}

	/**
	 * Update rendering with the camera state.
	 */
	void render(SceneShaderContext renderContext) {

		renderContext.setProjectionMatrix(projection)
		renderContext.setViewMatrix(view.mul(transform, viewTransform))
	}

	/**
	 * Convert a set of window coordinates to world coordinates.
	 *
	 * @param winX
	 * @param winY
	 * @param result A vector to store the result in.
	 * @return The {@code result} vector.
	 */
	Vector3f unproject(float winX, float winY, Vector3f result) {

		var viewport = window.viewport
		return getViewProjection()
			.unproject(
				winX,
				-winY,
				0,
				new int[]{ viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY() },
				result)
			.mul(1f, -1f, 1f)
	}
}
