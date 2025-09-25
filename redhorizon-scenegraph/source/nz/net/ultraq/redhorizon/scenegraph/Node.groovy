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


import org.joml.Vector3fc

/**
 * An element of a scene.
 *
 * @author Emanuel Rabina
 */
interface Node {

	/**
	 * Return the position of this node.
	 */
	Vector3fc getPosition()

	/**
	 * Set the position of this node.
	 */
	void setPosition(float x, float y, float z)

	/**
	 * Set the position of this node.
	 */
	default void setPosition(Vector3fc position) {

		setPosition(position.x(), position.y(), position.z())
	}

	/**
	 * Alter the position of this node through translation.
	 */
	void translate(float x, float y, float z)
}
