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
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.primitives.AABBf
import org.joml.primitives.AABBfc
import org.joml.primitives.Rectanglef

/**
 * An element of a scene.
 *
 * @author Emanuel Rabina
 */
class Node<T extends Node> {

	String name
	final List<Node> children = []
	Node parent
	protected final Vector3f _position = new Vector3f()
	protected final Rectanglef _boundingArea = new Rectanglef()
	protected final AABBf _boundingVolume = new AABBf()
	protected final Matrix4f _transform = new Matrix4f()

	/**
	 * Constructor, create a new node that takes up space in the scene.
	 */
	protected Node(float width = 0, float height = 0, float depth = 0) {

		_boundingVolume.set(0, 0, 0, width, height, depth)
		_boundingArea.set(0, 0, width, height)
	}

	/**
	 * Add a child node to this node.
	 */
	T addChild(Node child) {

		children.add(child)
		child.parent = this

		_boundingVolume.expand(child.boundingVolume)
		_boundingArea.expand(child.boundingArea)

		return (T)this
	}

	/**
	 * An overload of {@code <<} as an alias for {@link #addChild(Node)}.
	 */
	void leftShift(Node child) {

		addChild(child)
	}

	/**
	 * Return the bounding area of this node.
	 */
	Rectanglef getBoundingArea() {

		return _boundingArea
	}

	/**
	 * Return the bounding volume of this node.
	 */
	AABBfc getBoundingVolume() {

		return _boundingVolume
	}

	/**
	 * Get the depth of this node.
	 */
	float getDepth() {

		return _boundingVolume.lengthZ()
	}

	/**
	 * Get the height of this node.
	 */
	float getHeight() {

		return _boundingVolume.lengthY()
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

		return _transform.getTranslation(_position)
	}

	/**
	 * Walk up the scene graph to locate and return the scene to which this node
	 * belongs.
	 */
	protected Scene getScene() {

		return parent?.getScene()
	}

	/**
	 * Get the local transform of this node.
	 */
	Matrix4fc getTransform() {

		return _transform
	}

	/**
	 * Get the width of this node.
	 */
	float getWidth() {

		return _boundingVolume.lengthX()
	}

	/**
	 * Remove a child node from this node.
	 */
	T removeChild(Node child) {

		children.remove(child)
		child.parent = null

		var width = getWidth()
		var height = getHeight()
		var depth = getDepth()
		var position = getPosition()
		_boundingVolume.set(0, 0, 0, width, height, depth).translate(position)
		_boundingArea.set(0, 0, width, height).translate(position.x(), position.y())
		children.each { remainingChild ->
			_boundingVolume.expand(remainingChild.boundingVolume)
			_boundingArea.expand(remainingChild.boundingArea)
		}

		return (T)this
	}

	/**
	 * Set the position of this node.
	 */
	void setPosition(float x, float y, float z) {

		_transform.setTranslation(x, y, z)
		_boundingVolume.set(0, 0, 0, width, height, depth).translate(x, y, z)
		_boundingArea.set(0, 0, width, height).translate(x, y)
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

		_transform.translate(x, y, z)
		_boundingVolume.translate(x, y, z)
		_boundingArea.translate(x, y)
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
