/*
 * Copyright 2017, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph

import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Scriptable

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Rectanglef

import java.util.concurrent.CopyOnWriteArrayList

/**
 * An element of a scene, nodes are used to build and organize scene trees.
 *
 * @author Emanuel Rabina
 */
class Node<T extends Node> implements SceneEvents, Scriptable<T>, Visitable {

	final Matrix4f transform = new Matrix4f()
	final Rectanglef bounds = new Rectanglef()

	String name
	Node parent
	CopyOnWriteArrayList<Node> children = new CopyOnWriteArrayList<>()

	private final Vector3f position = new Vector3f()
	private final Vector3f scale = new Vector3f(1, 1, 1)
	private final Matrix4f globalTransform = new Matrix4f()
	private final Vector3f globalPosition = new Vector3f()
	private final Vector3f globalScale = new Vector3f(1, 1, 1)
	private final Rectanglef globalBounds = new Rectanglef()

	@Override
	void accept(SceneVisitor visitor) {

		visitor.visit(this)
		children*.accept(visitor)
	}

	/**
	 * Adds a child node to this node.
	 */
	T addChild(Node child) {

		children << child
		child.parent = this
		return this
	}

	/**
	 * Adds a child node to this node, shifting any existing nodes at the given
	 * position to the right to make room.
	 */
	T addChild(int index, Node child) {

		children.add(index, child)
		child.parent = this
		return this
	}

	/**
	 * Return the world-space bounds of this node.  ie: the local bounds, then
	 * taking into account local and all parent/ancestor transforms along the path
	 * to this node.
	 */
	Rectanglef getGlobalBounds() {

		var scale = getGlobalScale()
		var translate = getGlobalPosition()
		return globalBounds.set(bounds)
			.scale(scale.x, scale.y)
			.translate(translate.x, translate.y)
	}

	/**
	 * Get the world-space position of this node.  ie: the local position, then
	 * modified by all of the ancestor transforms along the path to this node.
	 */
	protected Vector3f getGlobalPosition() {

		return getGlobalTransform().getTranslation(globalPosition)
	}

	/**
	 * Get the world-space scale of this node.  ie: the local scale, then modified
	 * by all of the ancestor transforms along the path to this node.
	 */
	protected Vector3f getGlobalScale() {

		return globalTransform.getScale(globalScale)
	}

	/**
	 * Get the world-space transform of this node.  ie: the local transform, then
	 * modified by all of the ancestor transforms along the path to this node.
	 */
	protected Matrix4f getGlobalTransform() {

		return parent != null ?
			transform.mul(parent.globalTransform, globalTransform) :
			transform.get(globalTransform)
	}

	/**
	 * Returns this node's name.  Used for the scene overview and debugging,
	 * defaults to the class name of the node.
	 */
	String getName() {

		return name ?: this.class.simpleName
	}

	/**
	 * Get the local position of this node.  Note that the returned vector is a
	 * live value of this node's position, so be sure to wrap in your own object
	 * if you need a stable value.
	 */
	Vector3f getPosition() {

		var currentScale = getScale()
		return transform.getTranslation(position).div(currentScale)
	}

	/**
	 * Get the local scale of this node.  Note that the returnved vector is a live
	 * value of this node's scale, so be sure to wrap it in your own vector if you
	 * need a stable value.
	 */
	Vector3f getScale() {

		return transform.getScale(scale)
	}

	/**
	 * Test this element against a view frustum to check whether it is inside it
	 * or not.  Used for object culling during the rendering of a scene.
	 */
	boolean isVisible(FrustumIntersection frustumIntersection) {

		return frustumIntersection.testPlaneXY(getGlobalBounds())
	}

	/**
	 * Overload of the {@code <<} operator as an alias for
	 * {@link #addChild(Node)}.
	 */
	void leftShift(Node child) {

		addChild(child)
	}

	/**
	 * Set the local position of this node.
	 */
	void setPosition(Vector3f newPosition) {

		setPosition(newPosition.x, newPosition.y, newPosition.z)
	}

	/**
	 * Set the local position of this node.
	 */
	void setPosition(Vector2f newPosition) {

		setPosition(newPosition.x, newPosition.y)
	}

	/**
	 * Set the local position of this node.
	 */
	void setPosition(float x, float y, float z = 0) {

		var currentScale = getScale()
		transform.setTranslation(
			x * currentScale.x as float,
			y * currentScale.y as float,
			z * currentScale.z as float
		)
	}

	/**
	 * Set the local scale of this node.
	 */
	void setScale(Vector3f newScale) {

		setScale(newScale.x, newScale.y, newScale.z)
	}

	/**
	 * Set the local scale of this node.
	 */
	void setScale(float x, float y, float z = 1) {

		var currentScale = getScale()
		transform.scaleLocal(
			x / currentScale.x as float,
			y / currentScale.y as float,
			z / currentScale.z as float)
	}

	/**
	 * Set the scale of the X and Y axes to the same value.
	 */
	void setScaleXY(float newScale) {

		setScale(newScale, newScale)
	}
}
