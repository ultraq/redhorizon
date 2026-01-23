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
class Node<T extends Node> implements LocalTransform<T>, Named<T> {

	protected Node parent
	final List<Node> children = new CopyOnWriteArrayList<>()

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
		scene.trigger(new NodeAddedEvent(child))
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
	 * Return this node's parent, or {@code null} if it has none.
	 */
	Node getParent() {

		return parent
	}

	/**
	 * Walk up the scene graph to locate and return the scene to which this node
	 * belongs.
	 */
	Scene getScene() {

		return parent?.getScene()
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
		scene.trigger(new NodeRemovedEvent(child))
		return (T)this
	}

	/**
	 * Traverse this node and all of its children.
	 */
	void traverse(SceneVisitor<Node> visitor) {

		visitor.visit(this)
		children*.traverse(visitor)
	}

	/**
	 * Traverse this node and all of its children, but only if they are the same
	 * type as {@code type}.
	 */
	<T extends Node> void traverse(Class<T> type, SceneVisitor<T> visitor) {

		if (type.isInstance(this)) {
			visitor.visit((T)this)
		}
		children*.traverse(type, visitor)
	}
}
