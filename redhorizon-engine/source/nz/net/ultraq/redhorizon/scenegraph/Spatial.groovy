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

import org.joml.Rayf
import org.joml.Vector3f

/**
 * Using jMonkeyEngine's word for it, a 'spatial' is an item that can exist
 * within the scene in that it has a position, vector, rotation, scale, etc,
 * each of which can be manipulated.
 * 
 * @author Emanuel Rabina
 */
abstract class Spatial implements SceneElement {

	// Rotation limits
	public static final float ROTATION_MIN = 0.0f
	public static final float ROTATION_MAX = 360.0f

	// Transformation attributes
	protected Vector3f position = new Vector3f()
	protected float rotation = ROTATION_MIN

	/**
	 * Calculates the bounding volume of this object.
	 * 
	 * @return Bounding volume of this object.
	 */
	abstract BoundingVolume getBoundingVolume()

	/**
	 * Return whether or not this object is intersected by the given ray.
	 * 
	 * @param ray
	 * @return <tt>true</tt> if the bounding volume of this object is
	 * 		   intersected by the ray.
	 */
	abstract boolean intersects(Rayf ray)

	/**
	 * Sets the rotation of this object.  Rotation values outside of the range
	 * of {@link #ROTATION_MIN} and {@link #ROTATION_MAX} will be clamped.
	 * 
	 * @param rotation
	 */
	void setRotation(float rotation) {

		this.rotation = Math.clamp(rotation, ROTATION_MIN, ROTATION_MAX)
	}
}
