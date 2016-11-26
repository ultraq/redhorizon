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

package nz.net.ultraq.redhorizon.geometry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A 3-tuple, representing any 3-dimensional value.
 * 
 * @author Emanuel Rabina
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
	name = "Vector3f",
	propOrder = {"x","y","z"}
)
public class Vector3f {

	public static final Vector3f ZERO = new Vector3f();

	@XmlAttribute public float x;
	@XmlAttribute public float y;
	@XmlAttribute public float z;

	/**
	 * Default constructor, creates a new vector at (0,0,0).
	 */
	public Vector3f() {

		this(0, 0, 0);
	}

	/**
	 * Constructor, creates a new vectory with the given values.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3f(float x, float y, float z) {

		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Compare against another vector to see if they're equal.
	 * 
	 * @param obj
	 * @return <tt>true</tt> if the vectors share the same x, y, and z values.
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Vector3f) {
			Vector3f other = (Vector3f)obj;
			return x == other.x && y == other.y && z == other.z;
		}
		return false;
	}

	/**
	 * Returns the X value of this vector.  Primarily used by frameworks which
	 * expect Java bean methods - use the publicly accessible value instead.
	 * 
	 * @return X value.
	 */
	public float getX() {

		return x;
	}

	/**
	 * Returns the Y value of this vector.  Primarily used by frameworks which
	 * expect Java bean methods - use the publicly accessible value instead.
	 * 
	 * @return Y value.
	 */
	public float getY() {

		return y;
	}

	/**
	 * Returns the Z value of this vector.  Primarily used by frameworks which
	 * expect Java bean methods - use the publicly accessible value instead.
	 * 
	 * @return Z value.
	 */
	public float getZ() {

		return z;
	}

	/**
	 * Redefines <tt>hashCode()</tt> so that equal vectors have the same hash
	 * value.
	 * 
	 * @return Hash value for this vector.
	 */
	@Override
	public int hashCode() {

		return (int)(x * y * z);
	}

	/**
	 * Set the x value of this vector.  Primarily used by frameworks which
	 * expect Java bean methods - set the publicly accessible value instead.
	 * 
	 * @param x
	 */
	public void setX(float x) {

		this.x = x;
	}

	/**
	 * Set the y value of this vector.  Primarily used by frameworks which
	 * expect Java bean methods - set the publicly accessible value instead.
	 * 
	 * @param y
	 */
	public void setY(float y) {

		this.y = y;
	}

	/**
	 * Set the z value of this vector.  Primarily used by frameworks which
	 * expect Java bean methods - set the publicly accessible value instead.
	 * 
	 * @param z
	 */
	public void setZ(float z) {

		this.z = z;
	}

	/**
	 * Returns this vector as an array.  Used primarily for OpenGL/AL array
	 * functions.
	 * 
	 * @return [x, y, z]
	 */
	public float[] toArray() {

		return new float[]{x, y, z};
	}

	/**
	 * Returns this vector represented as (x,y,z).
	 * 
	 * @return Vector3f(x,y,z).
	 */
	@Override
	public String toString() {

		return "Vector3f(" + x + "," + y + "," + z + ")";
	}
}
