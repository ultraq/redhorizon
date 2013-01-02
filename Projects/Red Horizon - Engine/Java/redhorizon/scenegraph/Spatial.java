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
 * Using jMonkeyEngine's word for it, a 'spatial' is an item that can exist
 * within the scene in that it has a position, vector, rotation, scale, etc,
 * each of which can be manipulated.
 * 
 * @author Emanuel Rabina
 */
public abstract class Spatial {

	// Rotation limits
	public static final float ROTATION_MIN = 0.0f;
	public static final float ROTATION_MAX = 360.0f;

	// Transformation attributes
	protected Vector3f position = new Vector3f();
	protected float rotation = ROTATION_MIN;

	/**
	 * Calculates the bounding volume of this object.
	 * 
	 * @return Bounding volume of this object.
	 */
	public abstract BoundingVolume boundingVolume();

	/**
	 * Returns the position of this object.
	 * 
	 * @return Object position.
	 */
	public Vector3f getPosition() {

		return position;
	}

	/**
	 * Returns the rotation of this object.  Rotation is a range between the
	 * values of {@link #ROTATION_MIN} and {@link #ROTATION_MAX}.
	 * 
	 * @return Object rotation.
	 */
	public float getRotation() {

		return rotation;
	}

	/**
	 * Return whether or not this object is intersected by the given ray.
	 * 
	 * @param ray
	 * @return <tt>true</tt> if the bounding volume of this object is
	 * 		   intersected by the ray.
	 */
	public abstract boolean intersects(Ray ray);

	/**
	 * Set the position of this object.
	 * 
	 * @param position
	 */
	public void setPosition(Vector3f position) {

		this.position = position;
	}

	/**
	 * Sets the rotation of this object.  Rotation values outside of the range
	 * of {@link #ROTATION_MIN} and {@link #ROTATION_MAX} will be clamped.
	 * 
	 * @param rotation
	 */
	public void setRotation(float rotation) {

		this.rotation = Math.min(Math.max(rotation, ROTATION_MIN), ROTATION_MAX);
	}
}
