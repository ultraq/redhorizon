/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.audio

import nz.net.ultraq.redhorizon.geometry.Orientation
import nz.net.ultraq.redhorizon.geometry.Ray
import nz.net.ultraq.redhorizon.geometry.Vector3f
import nz.net.ultraq.redhorizon.scenegraph.BoundingBox
import nz.net.ultraq.redhorizon.scenegraph.BoundingVolume
import nz.net.ultraq.redhorizon.scenegraph.Spatial

/**
 * The player's ears into the world.
 * 
 * @author Emanuel Rabina
 */
class Listener extends Spatial {

	private Vector3f velocity = new Vector3f()
	private Orientation orientation = new Orientation()

	final BoundingVolume boundingVolume = BoundingBox.ZERO

	/**
	 * Listeners never intersect anything.
	 * 
	 * @param ray
	 * @return {@code false}
	 */
	@Override
	boolean intersects(Ray ray) {

		return false
	}

	/**
	 * Set/update the listener in the environment.
	 * 
	 * @param renderer
	 */
	void render(AudioRenderer renderer) {

		renderer.updateListener(this)
	}
}
