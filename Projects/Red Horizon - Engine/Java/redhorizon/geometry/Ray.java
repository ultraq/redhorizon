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

package redhorizon.geometry;

/**
 * A line segment in 3-dimensional space, which is a point of origin and a
 * direction in which the ray is headed.
 * 
 * @author Emanuel Rabina
 */
public class Ray {

	public Vector3f origin;
	public Vector3f direction;

	/**
	 * Constructor, creates a new ray with the given origin and direction.
	 * 
	 * @param origin
	 * @param direction
	 */
	public Ray(Vector3f origin, Vector3f direction) {

		this.origin    = origin;
		this.direction = direction;
	}
}
