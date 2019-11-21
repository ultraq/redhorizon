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

package nz.net.ultraq.redhorizon.scenegraph

import nz.net.ultraq.redhorizon.geometry.Ray
import nz.net.ultraq.redhorizon.geometry.Vector3f

import groovy.transform.TupleConstructor

/**
 * An object that represents a space that encompasses a set of objects.  Used
 * for making quick calculations of whether groups intersect with other groups
 * or objects.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class BoundingVolume {

	Vector3f center
	float width
	float height
	float depth

	/**
	 * Determine if a ray intersects this bounding volume.
	 * 
	 * @param ray Ray to test against
	 * @return <tt>true</tt> if this volume is intersected by the ray.
	 */
	boolean intersects(Ray ray) {

		return false
	}

	/**
	 * Combines this bounding volume with another, returning a new bounding
	 * volume that could contain both items within it.
	 * 
	 * @param volume
	 * @return A bounding volume that encompasses both this volume and the given
	 * 		   volume.
	 */
	BoundingVolume merge(BoundingVolume volume) {

		return new BoundingVolume(
			new Vector3f(
				(center.x + volume.center.x) / 2 as float,
				(center.y + volume.center.y) / 2 as float,
				(center.z + volume.center.z) / 2 as float
			),
			Math.max(center.x + (width / 2), volume.center.x + (volume.width / 2)) - Math.min(center.x - (width / 2), volume.center.x - (volume.width / 2)) as float,
			Math.max(center.y + (height / 2), volume.center.y + (volume.height / 2)) - Math.min(center.y - (height / 2), volume.center.y - (volume.height / 2)) as float,
			Math.max(center.z + (depth / 2), volume.center.z + (volume.depth / 2)) - Math.min(center.z - (depth / 2), volume.center.z - (volume.depth / 2)) as float
		)
	}
}
