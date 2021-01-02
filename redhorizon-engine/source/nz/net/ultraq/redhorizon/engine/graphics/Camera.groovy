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

import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

/**
 * A representation of the player's view into the world.
 * 
 * @author Emanuel Rabina
 */
class Camera implements GraphicsElement, SelfVisitable {

	// TODO: Maybe use a projection matrix (Matrix4f) for all of the camera
	//       properties instead?

	private static final int TICK = 48

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	@Override
	void init(GraphicsRenderer renderer) {
	}

	/**
	 * Move the camera down (negative along the Y axis) 1 tick.
	 */
	void moveDown() {

		position.add(0, -TICK, 0)
	}

	/**
	 * Move the camera left (negative along the X axis) 1 tick.
	 */
	void moveLeft() {

		position.add(-TICK, 0, 0)
	}

	/**
	 * Move the camera right (positive along the X axis) 1 tick.
	 */
	void moveRight() {

		position.add(TICK, 0, 0)
	}

	/**
	 * Move the camera up (positive along the Y axis) 1 tick.
	 */
	void moveUp() {

		position.add(0, TICK, 0)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		renderer.updateCamera(position)
	}
}
