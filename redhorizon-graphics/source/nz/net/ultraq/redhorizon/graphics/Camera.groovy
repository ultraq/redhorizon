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
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * The player's view into the world.
 *
 * @author Emanuel Rabina
 */
class Camera extends Node<Camera> {

	private final Window window
	final Matrix4f projection
	final Matrix4f view
	private final Matrix4f viewProjection = new Matrix4f()
	private final Vector3f position = new Vector3f()

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

	@Override
	Vector3fc getPosition() {

		return view.getTranslation(position)
	}

	@Override
	void setPosition(float x, float y, float z) {

		view.setTranslation(x, y, z)
	}

	@Override
	Camera translate(float x, float y, float z) {

		view.translate(-x, -y, -z)
		return this
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
//		var renderScale = window.renderScale
		return projection.mulAffine(view, viewProjection)
			.unproject(
//				winX / renderScale as float,
//				winY / renderScale as float,
				winX,
				winY,
				0,
				new int[]{ viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY() },
				result)
//			.mul(renderScale)
			.mul(1f, -1f, 1f)
	}

	/**
	 * Update rendering with the camera state.
	 */
	void update(SceneShaderContext renderContext) {

		renderContext.setProjectionMatrix(projection)
		renderContext.setViewMatrix(view)
	}
}
