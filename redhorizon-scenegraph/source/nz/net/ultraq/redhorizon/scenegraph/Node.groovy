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

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.CopyOnWriteArrayList

/**
 * An element of a scene.
 *
 * @author Emanuel Rabina
 */
class Node<T extends Node> implements Named<T>, Visitable {

	protected Node parent
	final List<Node> children = new CopyOnWriteArrayList<>()

	protected final Vector3f _position = new Vector3f()
	protected final Rectanglef _boundingArea = new Rectanglef()
	protected final AABBf _boundingVolume = new AABBf()
	protected final Matrix4f _transform = new Matrix4f()
	protected final Matrix4f _globalTransform = new Matrix4f()
	protected final Vector3f _globalPosition = new Vector3f()

	/**
	 * Default constructor, create a new node that takes up no space in the scene.
	 */
	Node() {

		this(0, 0, 0)
	}

	/**
	 * Constructor, create a new node that takes up space in the scene.
	 */
	Node(float width, float height, float depth) {

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
	 * Locate the first ancestor node that satisfies the given predicate.
	 *
	 * @param predicate
	 * @return
	 *   The matching node, or {@code null} if no match is found.
	 */
	Node findAncestor(@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.scenegraph.Node') Closure<Boolean> predicate) {

		if (parent) {
			return predicate(parent) ? parent : parent.findAncestor(predicate)
		}
		return null
	}

	/**
	 * Locate the first descendent from this node that satisfies the given
	 * predicate.
	 *
	 * @param predicate
	 * @return
	 *   The matching node, or {@code null} if no match is found.
	 */
	Node findDescendent(@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.scenegraph.Node') Closure<Boolean> predicate) {

		return children.find { node ->
			return predicate(node) ? node : node.findDescendent(predicate)
		}
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
	 * Return the global position of this node.  That is, the local position
	 * multiplied by every local position of the node's ancestors.
	 */
	Vector3f getGlobalPosition() {

		return globalTransform.getTranslation(_globalPosition)
	}

	/**
	 * Return the global transform of this node.  That is, the local transform
	 * multiplied by every local transform of the node's ancestors.
	 */
	Matrix4f getGlobalTransform() {

		_globalTransform.set(transform)
		if (parent) {
			_globalTransform.mul(parent.globalTransform)
		}
		return _globalTransform
	}

	/**
	 * Get the height of this node.
	 */
	float getHeight() {

		return _boundingVolume.lengthY()
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
	Scene getScene() {

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
	 * An overload of {@code <<} as an alias for {@link #addChild(Node)}.
	 */
	void leftShift(Node child) {

		addChild(child)
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

	@Override
	void traverse(SceneVisitor visitor) {

		visitor.visit(this)
		children*.traverse(visitor)
	}

	/**
	 * Set the position of and return this node.
	 */
	T withPosition(float x, float y, float z) {

		setPosition(x, y, z)
		return (T)this
	}
}
