/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * Any component with a transform matrix to describe local changes to its
 * position, rotation and scale.
 *
 * @author Emanuel Rabina
 */
trait LocalTransform<T extends LocalTransform> {

	private final Matrix4f transform = new Matrix4f()
	private final Vector3f positionResult = new Vector3f()
	private final Vector3f rotationResult = new Vector3f()
	private final Vector3f scaleResult = new Vector3f()

	/**
	 * Return the position of this object.
	 */
	Vector3fc getPosition() {

		return transform.getTranslation(positionResult)
	}

	/**
	 * Return the rotation of this object.
	 */
	Vector3fc getRotation() {

		return transform.getEulerAnglesXYZ(rotationResult)
	}

	/**
	 * Return the scale of this object.
	 */
	Vector3fc getScale() {

		return transform.getScale(scaleResult)
	}

	/**
	 * Return a read-only view of this object's transform.
	 */
	Matrix4fc getTransform() {

		return transform
	}

	/**
	 * Reset the transform of this object.
	 */
	T resetTransform() {

		transform.identity()
		return (T)this
	}

	/**
	 * Adjust the rotation of this object.
	 */
	T rotate(float x, float y, float z) {

		transform.rotateXYZ(x, y, z)
		return (T)this
	}

	/**
	 * Adjust the scale of this object.
	 */
	T scale(float x, float y, float z = 1f) {

		transform.scale(x, y, z)
		return (T)this
	}

	/**
	 * Adjust the scale of this object by the same value for all 3 dimensions.
	 */
	T scale(float xyz) {

		return scale(xyz, xyz, xyz)
	}

	/**
	 * Set the position of this object.
	 */
	T setPosition(float x, float y, float z = 0f) {

		transform.setTranslation(x, y, z)
		return (T)this
	}

	/**
	 * Set the rotation of this object.
	 */
	T setRotation(float x, float y, float z) {

		transform.setRotationXYZ(x, y, z)
		return (T)this
	}

	/**
	 * Replace the transform of this object with another one.
	 */
	T setTransform(Matrix4fc fromTransform) {

		transform.set(fromTransform)
		return (T)this
	}

	/**
	 * Adjust the position of this object.
	 */
	T translate(float x, float y, float z = 0f) {

		transform.translate(x, y, z)
		return (T)this
	}
}
