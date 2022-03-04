/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.geometry.Dimension

import org.joml.Matrix4f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A representation of the player's view into the world.
 * 
 * @author Emanuel Rabina
 */
class Camera implements GraphicsElement {

	private static final Logger logger = LoggerFactory.getLogger(Camera)

	final Dimension size
	final Matrix4f projection
	final Matrix4f view = new Matrix4f()
	private boolean moved = false

	/**
	 * Constructor, build a camera to work with the given dimensions.
	 * 
	 * @param renderResolution
	 */
	Camera(Dimension renderResolution) {

		size = new Dimension(renderResolution.width, renderResolution.height)
		projection = new Matrix4f()
			.ortho2D(-size.width / 2, size.width / 2, -size.height / 2, size.height / 2)
			.lookAt(
				new Vector3f(0, 0, 1),
				new Vector3f(),
				new Vector3f(0, 1, 0)
			)
		logger.debug('Establishing an orthographic projection of {}', size)
	}

	/**
	 * Move the camera so it centers the given point in the view.
	 * 
	 * @param point
	 * @return
	 */
	Camera center(Vector3f point) {

		return center(point.x, point.y, point.z)
	}

	/**
	 * Move the camera so it centers the given point in the view.
	 * 
	 * @param point
	 * @return
	 */
	Camera center(float x, float y, float z = 0) {

		view.translate(view.origin(new Vector3f()).sub(x, y, z))
		moved = true
		return this
	}

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	@Override
	void init(GraphicsRenderer renderer) {

		renderer.createCamera(projection, view)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (moved) {
			renderer.updateCamera(view)
			moved = false
		}
	}

	/**
	 * Reset this camera's scale.
	 * 
	 * @return
	 */
	Camera resetScale() {

		def scale = view.getScale(new Vector3f())
		view.scaleLocal(1 / scale.x as float, 1 / scale.y as float, 1)
		moved = true
		return this
	}

	/**
	 * Scale the camera view as a way of altering the perceived zoom.
	 * 
	 * @param factor
	 * @return
	 */
	Camera scale(float factor) {

		view.scaleLocal(factor, factor, 1)
		moved = true
		return this
	}

	/**
	 * Translates the position of this camera.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	Camera translate(float x, float y, float z = 0) {

		def scale = view.getScale(new Vector3f())
		view.translate(x / scale.x as float, y / scale.y as float, z)
		moved = true
		return this
	}
}
