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

import nz.net.ultraq.redhorizon.geometry.Dimension

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

	/**
	 * Constructor, build a camera to work with the given dimensions.
	 * 
	 * @param windowSize
	 * @param fixAspectRatio
	 */
	Camera(Dimension windowSize, boolean fixAspectRatio) {

		size = new Dimension(
			windowSize.width,
			(fixAspectRatio ? windowSize.height / 1.2 : windowSize.height) as int
		)
		projection = new Matrix4f()
			.ortho2D(-size.width / 2, size.width / 2, -size.height / 2, size.height / 2)
			.lookAt(
				new Vector3f(0, 0, 1),
				new Vector3f(),
				new Vector3f(0, 1, 0)
			)
		logger.debug('Establishing an orthographic projection of {}', size)
	}

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	@Override
	void init(GraphicsRenderer renderer) {

		renderer.createCamera(projection)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.updateCamera(view)
	}
}
