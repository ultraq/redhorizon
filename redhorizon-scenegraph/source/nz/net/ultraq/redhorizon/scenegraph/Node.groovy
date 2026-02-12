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

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.CopyOnWriteArrayList

/**
 * An element of a scene.
 *
 * @author Emanuel Rabina
 */
class Node<T extends Node> implements AutoCloseable {

	final List<Node> children = new CopyOnWriteArrayList<>()
	protected Node parent
	protected String name
	private boolean enabled = true
	private final Matrix4f transform = new Matrix4f()
	private final Vector3f positionResult = new Vector3f()
	private final Vector3f rotationResult = new Vector3f()
	private final Vector3f scaleResult = new Vector3f()
	private final Matrix4f globalTransformResult = new Matrix4f()
	private final Vector3f globalPositionResult = new Vector3f()
	private final Vector3f globalRotationResult = new Vector3f()
	private final Vector3f globalScaleResult = new Vector3f()

	/**
	 * Add and return the child node to this node.
	 */
	@SuppressWarnings('GrUnnecessaryPublicModifier')
	public <T extends Node<T>> T addAndReturnChild(T child) {

		addChild(child)
		return child
	}

	/**
	 * Add a child node to this node.
	 */
	T addChild(Node child) {

		children.add(child)
		child.parent = this
		scene?.trigger(new NodeAddedEvent(child))
		return (T)this
	}

	/**
	 * Remove all children from this node.
	 */
	void clear() {

		children.each { child ->
			child.parent = null
		}
		children.clear()
	}

	@Override
	void close() {

		children.each { child ->
			if (child instanceof AutoCloseable) {
				child.close()
			}
		}
	}

	/**
	 * Disable this object.
	 */
	T disable() {

		enabled = false
		return (T)this
	}

	/**
	 * Enable this object.
	 */
	T enable() {

		enabled = true
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
	 * @return The matching node, or {@code null} if no match is found.
	 */
	<T extends Node> T find(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.scenegraph.Node')
			Closure<Boolean> predicate) {

		return (T)children.find { node ->
			return predicate(node) ? node : node.find(predicate)
		}
	}

	/**
	 * Locate the first descendent from this node that satisfies the given name.
	 *
	 * @return The matching node, or {@code null} if no match is found.
	 */
	<T extends Node> T findByName(String name) {

		return (T)children.find { node ->
			return node.name == name
		}
	}

	/**
	 * Locate the first descendent from this node that satisfies the given type.
	 *
	 * @return The matching node, or {@code null} if no match is found.
	 */
	<T extends Node> T findByType(Class<T> type) {

		return (T)children.find { node ->
			return type.isInstance(node) ? node : node.findByType(type)
		}
	}

	/**
	 * Return the global position of this node.  That is, the local position
	 * multiplied by every local position of the node's ancestors.
	 */
	Vector3fc getGlobalPosition() {

		return globalTransform.getTranslation(globalPositionResult)
	}

	/**
	 * Return the global rotation of this node.  That is, the local rotation
	 * multiplied by every local rotation of the node's ancestors.
	 */
	Vector3fc getGlobalRotation() {

		return globalTransform.getEulerAnglesXYZ(globalRotationResult)
	}

	/**
	 * Return the global scale of this node.  That is, the local scale multiplied
	 * by every local scale of the node's ancestors.
	 */
	Vector3fc getGlobalScale() {

		return globalTransform.getScale(globalScaleResult)
	}

	/**
	 * Return the global transform of this node.  That is, the local transform
	 * multiplied by every local transform of the node's ancestors.
	 */
	Matrix4fc getGlobalTransform() {

		return parent ?
			parent.globalTransform.mul(transform, globalTransformResult) :
			transform.get(globalTransformResult)
	}

	/**
	 * Returns this object's name, defaulting to the class name if not set using
	 * {@link #withName(String)}.
	 */
	String getName() {

		return name ?: this.class.simpleName
	}

	/**
	 * Return this node's parent, or {@code null} if it has none.
	 */
	Node getParent() {

		return parent
	}

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
	 * Walk up the scene graph to locate and return the scene to which this node
	 * belongs.
	 */
	Scene getScene() {

		return parent?.getScene()
	}

	/**
	 * Return a read-only view of this object's transform.
	 */
	Matrix4fc getTransform() {

		return transform
	}

	/**
	 * Insert a child node before another child node.
	 */
	T insertBefore(Node child, Node before) {

		var index = children.indexOf(before)
		if (index != -1) {
			children.add(index, child)
			child.parent = this
		}
		return (T)this
	}

	/**
	 * Return whether this object is disabled.
	 */
	boolean isDisabled() {

		return !enabled
	}

	/**
	 * Return whether this object is enabled.
	 */
	boolean isEnabled() {

		return enabled
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
		scene?.trigger(new NodeRemovedEvent(child))
		return (T)this
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

	/**
	 * Traverse this node and all of its children.
	 */
	void traverse(SceneVisitor visitor) {

		if (visitor.visit(this)) {
			children*.traverse(visitor)
		}
	}

	/**
	 * Traverse this node and all of its children, but only if they are the same
	 * type as {@code type}.
	 */
	<T extends Node> void traverse(Class<T> type, SceneVisitor visitor) {

		if (type.isInstance(this)) {
			if (visitor.visit(this)) {
				children*.traverse(type, visitor)
			}
		}
		else {
			children*.traverse(type, visitor)
		}
	}

	/**
	 * Set the name of this object.
	 */
	T withName(String name) {

		this.name = name
		return (T)this
	}
}
