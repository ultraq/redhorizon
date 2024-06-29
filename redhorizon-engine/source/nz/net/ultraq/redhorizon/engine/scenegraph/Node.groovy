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

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 * An element of a scene, nodes are used to build and organize scene trees.
 *
 * @author Emanuel Rabina
 */
class Node<T extends Node> implements SceneEvents, Scriptable<T> {

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
	private PartitionHint partitionHint = null

	/**
	 * Adds a child node to this node.
	 */
	T addChild(Node child) {

		children.add(child)
		child.parent = this

		// Allow it to process
		var scene = getScene()
		if (scene) {
			addNodeAndChildren(scene, child)
				.orTimeout(5, TimeUnit.SECONDS)
				.join()
		}

		var childPosition = child.position
		var childBounds = new Rectanglef(child.bounds).translate(childPosition.x, childPosition.y)
		if (bounds.lengthX() && bounds.lengthY()) {
			bounds.expand(childBounds)
		}
		else {
			bounds.set(childBounds)
		}

		return (T)this
	}

	/**
	 * Trigger the {@code onSceneAdded} event for this node and all its children.
	 * Each node triggers a {@link NodeAddedEvent} event.
	 */
	protected CompletableFuture<Void> addNodeAndChildren(Scene scene, Node node) {

		return CompletableFuture.allOf(
			node.onSceneAddedAsync(scene),
			node.script?.onSceneAddedAsync(scene) ?: CompletableFuture.completedFuture(null)
		)
			.thenRunAsync { ->
				scene.trigger(new NodeAddedEvent(node))
			}
			.thenComposeAsync { _ ->
				var futures = node.children.collect { childNode -> addNodeAndChildren(scene, childNode) }

				// Originally used the Groovy spread operator `*` but this would throw
				// an exception about "array length is not legal" 🤷
				return CompletableFuture.allOf(futures.toArray(new CompletableFuture<Void>[0]))
			}
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
	 * Locate the first node in the scene that satisfies the given predicate.
	 *
	 * @param predicate
	 * @return
	 *   The matching node, or {@code null} if no match is found.
	 */
	Node findNode(@ClosureParams(value = SimpleType, options = "Node") Closure<Boolean> predicate) {

		return children.find { node ->
			return predicate(node) ? node : node.findNode(predicate)
		}
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
	Vector3f getGlobalPosition() {

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
	 * defaulting to {@link PartitionHint#None} if there are no hints in the node's ancestor
	 * tree.
	 */
	PartitionHint getPartitionHint() {

		return partitionHint ?: parent?.partitionHint ?: PartitionHint.None
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
	 * Get the local scale of this node.  Note that the returned vector is a live
	 * value of this node's scale, so be sure to wrap it in your own vector if you
	 * need a stable value.
	 */
	Vector3f getScale() {

		return transform.getScale(scale)
	}

	/**
	 * Walk up the scene graph to locate and return the scene to which this node
	 * belongs.
	 */
	protected Scene getScene() {

		return parent?.getScene()
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

		var plane = getGlobalBounds()
		return frustumIntersection.testPlaneXY(plane.minX, plane.minY, plane.maxX, plane.maxY)
	}

	/**
	 * Overload of the {@code <<} operator as an alias for
	 * {@link #addChild(Node)}.
	 */
	void leftShift(Node child) {

		addChild(child)
	}

	/**
	 * Remove the child node from this one.
	 */
	T removeChild(Node node) {

		if (children.remove(node)) {
			removeNodeAndChildren(scene, node)
				.orTimeout(5, TimeUnit.SECONDS)
				.join()
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
	 * Trigger the {@code onSceneRemoved} handler for this node and all its
	 * children.  Each node triggers a {@link NodeRemovedEvent} event.
	 */
	protected CompletableFuture<Void> removeNodeAndChildren(Scene scene, Node node) {

		return CompletableFuture.allOf(
			node.onSceneRemovedAsync(scene),
			node.script?.onSceneRemovedAsync(scene) ?: CompletableFuture.completedFuture(null)
		)
			.thenRunAsync { ->
				scene.trigger(new NodeRemovedEvent(node))
			}
			.thenComposeAsync { _ ->
				var futures = node.children.collect { childNode -> removeNodeAndChildren(scene, childNode) }

				// Originally used the Groovy spread operator `*` but this would throw
				// an exception about "array length is not legal" 🤷
				return CompletableFuture.allOf(futures.toArray(new CompletableFuture<Void>[0]))
			}
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
	 * Called on every frame before the node is rendered, allowing it to perform
	 * any processing as a response to changes in the scene.
	 *
	 * @param delta
	 *   Time, in seconds, since the last time this method was called.
	 */
	void update(float delta) {
	}
}
