/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * An element of a scene.
 *
 * @author Emanuel Rabina
 */
class Node<T extends Node> {

	private final Vector3f position = new Vector3f()
	protected final Matrix4f transform = new Matrix4f()

	/**
	 * Return the position of this node.
	 */
	Vector3fc getPosition() {

		return transform.getTranslation(position)
	}

	/**
	 * Set the position of this node.
	 */
	void setPosition(float x, float y, float z) {

		transform.setTranslation(x, y, z)
	}

	/**
	 * Set the position of this node.
	 */
	void setPosition(Vector3fc position) {

		setPosition(position.x(), position.y(), position.z())
	}

	/**
	 * Alter the position of this node through translation.
	 */
	T translate(float x, float y, float z) {

		transform.translate(x, y, z)
		return (T)this
	}
}
