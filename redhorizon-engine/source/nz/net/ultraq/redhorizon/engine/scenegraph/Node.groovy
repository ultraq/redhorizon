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
import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.primitives.Rectanglef

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

/**
 * An element of a scene, nodes are used to build and organize scene trees.
 *
 * @author Emanuel Rabina
 */
class Node<T extends Node> implements SceneEvents, Scriptable<T> {

	String name
	Node parent
	CopyOnWriteArrayList<Node> children = new CopyOnWriteArrayList<>()
	NodeListDisplayHint nodeListDisplayHint = NodeListDisplayHint.START_EXPANDED

	private final Rectanglef bounds = new Rectanglef()
	private final Matrix4f transform = new Matrix4f()
	private final Vector3f position = new Vector3f()
	private final Vector3f scale = new Vector3f(1, 1, 1)
	private final Matrix4f globalTransform = new Matrix4f()
	private final Vector3f globalPosition = new Vector3f()
	private final Vector3f globalScale = new Vector3f(1, 1, 1)
	private final Rectanglef globalBounds = new Rectanglef()
	private PartitionHint partitionHint = null
	private UpdateHint updateHint = null

	/**
	 * Adds a child node to this node.
	 */
	T addChild(Node child) {

		children.add(child)
		child.parent = this

		// Allow it to process
		var scene = getScene()
		if (scene) {
			scene.addNodeAndChildren(child).join()
		}

		var childBounds = new Rectanglef(child.bounds).translate(child.position.x(), child.position.y())
		if (bounds.lengthX() && bounds.lengthY()) {
			bounds { ->
				expand(childBounds)
			}
		}
		else {
			bounds { ->
				set(childBounds)
			}
		}

		return (T)this
	}

	/**
	 * Modify the bounds of this node using the given closure.  The delegate of
	 * the closure will be the bounds themselves.
	 * <p>
	 * This only exists because JOML currently doesn't have a read-only view for
	 * {@code Rectanglef}, though hopefully that won't be the case if this PR gets
	 * merged: https://github.com/JOML-CI/joml-primitives/pull/3
	 */
	void bounds(@DelegatesTo(Rectanglef) Closure closure) {

		closure.delegate = bounds
		closure()
		recalculateProperties()
	}

	/**
	 * Remove all child nodes from this node.
	 */
	void clear() {

		children.each { child ->
			removeChild(child)
		}
	}

	/**
	 * Locate the first ancestor node that satisfies the given predicate.
	 *
	 * @param predicate
	 * @return
	 *   The matching node, or {@code null} if no match is found.
	 */
	Node findAncestor(@ClosureParams(value = SimpleType, options = 'Node') Closure<Boolean> predicate) {

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
	Node findDescendent(@ClosureParams(value = SimpleType, options = "Node") Closure<Boolean> predicate) {

		return children.find { node ->
			return predicate(node) ? node : node.findDescendent(predicate)
		}
	}

	/**
	 * Returns this node's bounds.  Don't modify this object directly, instead
	 * use {@link #bounds(Closure)} and the closure to make changes.
	 * <p>
	 * This only exists because JOML currently doesn't have a read-only view for
	 * {@code Rectanglef}, though hopefully that won't be the case if this PR gets
	 * merged: https://github.com/JOML-CI/joml-primitives/pull/3
	 */
	Rectanglef getBounds() {

		return bounds
	}

	/**
	 * Return the world-space bounds of this node.  ie: the local bounds, then
	 * taking into account local and all parent/ancestor transforms along the path
	 * to this node.
	 */
	Rectanglef getGlobalBounds() {

		return globalBounds
	}

	/**
	 * Get the world-space position of this node.  ie: the local position, then
	 * modified by all of the ancestor transforms along the path to this node.
	 */
	Vector3fc getGlobalPosition() {

		return globalPosition
	}

	/**
	 * Get the world-space scale of this node.  ie: the local scale, then modified
	 * by all of the ancestor transforms along the path to this node.
	 */
	Vector3fc getGlobalScale() {

		return globalScale
	}

	/**
	 * Get the world-space transform of this node.  ie: the local transform, then
	 * modified by all of the ancestor transforms along the path to this node.
	 */
	Matrix4fc getGlobalTransform() {

		return globalTransform
	}

	/**
	 * Return the height of the node.  This is a shortcut for calling
	 * {@code bounds.lengthY()}.
	 */
	float getHeight() {

		return bounds.lengthY()
	}

	/**
	 * Returns this node's name.  Used for the scene overview and debugging,
	 * defaults to the class name of the node.
	 */
	String getName() {

		return name ?: this.class.simpleName
	}

	/**
	 * A hint to the scenegraph to add this node to an appropriate data structure
	 * for performance purposes.
	 * <p>
	 * The default behaviour is to inherit the partition hint of its parent,
	 * defaulting to {@link PartitionHint#NONE} if there are no hints in the node's ancestor
	 * tree.
	 */
	PartitionHint getPartitionHint() {

		return partitionHint ?: parent?.partitionHint ?: PartitionHint.NONE
	}

	/**
	 * Get the local position of this node.
	 * <p>
	 * Note that the returned vector is a live value of this node's scale, so be
	 * sure to wrap it in your own vector if you need a stable value.
	 */
	Vector3fc getPosition() {

		return position
	}

	/**
	 * Get the local scale of this node.
	 * <p>
	 * Note that the returned vector is a live value of this node's scale, so be
	 * sure to wrap it in your own vector if you need a stable value.
	 */
	Vector3fc getScale() {

		return scale
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
	 * <p>
	 * Note that the returned matrix is a live value of this node's transform, so
	 * be sure to wrap it in your own matrix if you need a stable value.
	 */
	Matrix4fc getTransform() {

		return transform
	}

	/**
	 * A hint to the scenegraph to add this node to an appropriate data structure
	 * for performance purposes.
	 * <p>
	 * The default behaviour is to inherit the update hint of its parent,
	 * defaulting to {@link UpdateHint#ALWAYS} if there are no hints in the node's
	 * ancestor tree.
	 */
	UpdateHint getUpdateHint() {

		return updateHint ?: parent?.updateHint ?: UpdateHint.ALWAYS
	}

	/**
	 * Return the width of the node.  This is a shortcut for calling
	 * {@code bounds.lengthX()}.
	 */
	float getWidth() {

		return bounds.lengthX()
	}

	/**
	 * Test this element against a view frustum to check whether it is inside it
	 * or not.  Used for object culling during the rendering of a scene.
	 */
	boolean isVisible(FrustumIntersection frustumIntersection) {

		return frustumIntersection.testPlaneXY(globalBounds.minX, globalBounds.minY, globalBounds.maxX, globalBounds.maxY)
	}

	/**
	 * Overload of the {@code <<} operator as an alias for
	 * {@link #addChild(Node)}.
	 */
	void leftShift(Node child) {

		addChild(child)
	}

	/**
	 * Recalculate all of the node properties that hang off a transform change.
	 */
	protected void recalculateProperties() {

		// Update local properties
		transform.getScale(scale)
		transform.getTranslation(position).div(scale)

		// Update global properties
		if (parent) {
			transform.mul(parent.globalTransform, globalTransform)
		}
		else {
			transform.get(globalTransform)
		}

		globalTransform.getScale(globalScale)
		globalTransform.getTranslation(globalPosition)
		globalBounds.set(bounds)
			.scale(globalScale.x, globalScale.y)
			.translate(globalPosition.x, globalPosition.y)

		// Children need to be updated too
		children*.traverse { Node node ->
			node.recalculateProperties()
			return true
		}
	}

	/**
	 * Remove the child node from this one.
	 */
	T removeChild(Node node) {

		if (children.remove(node)) {
			var scene = getScene()
			if (scene) {
				scene.removeNodeAndChildren(node).join()
			}
			node.parent = null
			// TODO: Recalculate bounds
		}
		return (T)this
	}

	/**
	 * Remove any child node that satisfies the closure condition.
	 */
	T removeChild(Closure predicate) {

		children.each { node ->
			if (predicate(node)) {
				removeChild(node)
			}
		}
		return (T)this
	}

	/**
	 * Set the partition hint for this node.
	 */
	void setPartitionHint(PartitionHint partitionHint) {

		this.partitionHint = partitionHint
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

		transform.setTranslation(
			x * scale.x as float,
			y * scale.y as float,
			z * scale.z as float
		)
		recalculateProperties()
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

		transform.scaleLocal(
			x / scale.x() as float,
			y / scale.y() as float,
			z / scale.z() as float
		)
		recalculateProperties()
	}

	/**
	 * Set the scale of the X and Y axes to the same value.
	 */
	void setScaleXY(float newScale) {

		setScale(newScale, newScale)
	}

	/**
	 * Modify the transform of this node using the given closure.  The delegate of
	 * the closure will be the transform itself.
	 */
	void transform(@DelegatesTo(Matrix4f) Closure closure) {

		closure.delegate = transform
		closure()
		recalculateProperties()
	}

	/**
	 * Traverse a scene and control visits to any children.
	 * <p>
	 * The behaviour of a visitable element is slightly different from standard
	 * iteration in that the visitor can specify if it wishes to visit each of a
	 * node's children or not, based on the return value from the visit.
	 */
	void traverse(SceneVisitor visitor) {

		if (visitor.visit(this)) {
			children*.traverse(visitor)
		}
	}

	/**
	 * Similar to {@link #traverse}, but run in parallel.
	 */
	CompletableFuture<Void> traverseAsync(SceneVisitor visitor) {

		return CompletableFuture.supplyAsync { ->
			return visitor.visit(this)
		}
			.thenComposeAsync { visitChildren ->
				var futures = []
				if (visitChildren) {
					futures = children.collect { child -> child.traverseAsync(visitor) }
				}
				return CompletableFuture.allOf(futures.toArray(new CompletableFuture<Void>[0]))
			}
	}

	/**
	 * Called on every frame before the node is rendered, allowing it to perform
	 * any processing as a response to changes in the scene.
	 *
	 * @param delta
	 *   Time, in seconds, since the last time this method was called.
	 */
	void update(float delta) {
	}
}
