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

	String name
	final List<Node> children = []
	Node parent
	private final Vector3f position = new Vector3f()
	protected final Matrix4f transform = new Matrix4f()

	/**
	 * Add a child node to this node.
	 */
	T addChild(Node child) {

		children.add(child)
		child.parent = this
		return (T)this
	}

	/**
	 * An overload of {@code <<} as an alias for {@link #addChild(Node)}.
	 */
	void leftShift(Node child) {

		addChild(child)
	}

	/**
	 * Returns this node's name.  Used for the scene overview and debugging,
	 * defaults to the class name of the node.
	 */
	String getName() {

		return name ?: this.class.simpleName
	}

	/**
	 * Return the position of this node.
	 */
	Vector3fc getPosition() {

		return transform.getTranslation(position)
	}

	/**
	 * Walk up the scene graph to locate and return the scene to which this node
	 * belongs.
	 */
	protected Scene getScene() {

		return parent?.getScene()
	}

	/**
	 * Remove a child node from this node.
	 */
	T removeChild(Node child) {

		children.remove(child)
		child.parent = null
		return (T)this
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

	/**
	 * Traverse this node and all of its children.
	 */
	void traverse(SceneVisitor visitor) {

		visitor.visit(this)
		children*.traverse(visitor)
	}
}
