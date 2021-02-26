/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.scenegraph

/**
 * Trait for objects that can be scaled in the game world.
 * 
 * @author Emanuel Rabina
 */
trait Scalable {

	float scaleX = 1
	float scaleY = 1

	/**
	 * Provided both X and Y axes have the same scale factor, return their shared
	 * value.
	 * 
	 * @return
	 */
	float getScale() {

		if (scaleX != scaleY) {
			throw new Exception('x & y scale factors are different')
		}
		return scaleX
	}

	/**
	 * Apply the same scale factor to both axes.
	 * 
	 * @param xy
	 * @return
	 */
	void setScale(float xy) {

		scaleX = xy
		scaleY = xy
	}
}