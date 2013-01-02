/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.scenegraph;

import redhorizon.geometry.Ray;
import redhorizon.geometry.Vector3f;

/**
 * Common code used for each of the bounding types.
 * 
 * @author Emanuel Rabina
 */
public abstract class BoundingVolume {

	protected Vector3f center = Vector3f.ZERO;

	/**
	 * Determine if a ray intersects this bounding volume.
	 * 
	 * @param ray Ray to test against
	 * @return <tt>true</tt> if this volume is intersected by the ray.
	 */
	public abstract boolean intersects(Ray ray);

	/**
	 * Combines this bounding volume with another, returning a new bounding
	 * volume that could contain both items within it.
	 * 
	 * @param volume
	 * @return A bounding volume that encompasses both this volume and the given
	 * 		   volume.
	 */
	public abstract BoundingVolume merge(BoundingVolume volume);
}
