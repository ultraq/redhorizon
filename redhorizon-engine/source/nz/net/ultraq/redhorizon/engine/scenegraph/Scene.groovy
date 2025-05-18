/*
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.audio.AudioRequests
import nz.net.ultraq.redhorizon.engine.game.GameObject
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests
import nz.net.ultraq.redhorizon.engine.input.InputHandler
import nz.net.ultraq.redhorizon.engine.input.InputRequests
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Listener
import nz.net.ultraq.redhorizon.engine.scenegraph.partioning.QuadTree
import nz.net.ultraq.redhorizon.events.EventTarget

import org.joml.FrustumIntersection
import org.joml.Intersectionf
import org.joml.primitives.Circlef
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Semaphore

/**
 * Entry point for the Red Horizon scene graph, holds all of the objects that
 * make up the 'world'.
 *
 * @author Emanuel Rabina
 */
class Scene implements EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(Scene)

	// TODO: This stuff is really all 'scene/application context' objects, so
	//       should be moved to something as such 🤔
	@Delegate
	AudioRequests audioRequestsHandler
	@Delegate
	GraphicsRequests graphicsRequestHandler
	@Delegate
	InputRequests inputRequestHandler

	Camera camera
	Listener listener

	@Delegate(includes = ['clear', 'findAncestor', 'findDescendent', 'leftShift', 'removeChild', 'traverse', 'traverseAsync'], interfaces = false)
	final Node root = new RootNode(this)

	// Partition objects in the following data structures to make queries faster
	private final ConcurrentSkipListSet<Float> zValues = new ConcurrentSkipListSet<>()
	private final TreeMap<Float, QuadTree> quadTrees = new TreeMap<>()
	private final TreeMap<Float, CopyOnWriteArrayList<Node>> nodeLists = new TreeMap<>()
	private final List<GameObject> updateableNodes = new CopyOnWriteArrayList<>()
	private final List<InputHandler> inputNodes = new CopyOnWriteArrayList<>()
	private final Semaphore createQuadTreeSemaphore = new Semaphore(1)
	private final Semaphore createNodeListSemaphore = new Semaphore(1)

	/**
	 * Add a top-level node to this scene.  Shorthand for
	 * {@code scene.root.addNode(node)}.
	 */
	Scene addChild(Node node) {

		time('Adding node', logger) { ->
			root.addChild(node)
		}
		return this
	}

	/**
	 * Trigger the {@code onSceneAdded} event for this node and all its children.
	 * Each node triggers a {@link NodeAddedEvent} event.
	 */
	@PackageScope
	CompletableFuture<Void> addNodeAndChildren(Node node) {

		return CompletableFuture.allOf(
			node.onSceneAddedAsync(this),
			node.script?.onSceneAddedAsync(this) ?: CompletableFuture.completedFuture(null)
		)
			.thenRunAsync { ->
				partition(node)
				trigger(new NodeAddedEvent(node))
			}
			.thenComposeAsync { _ ->
				return CompletableFuture.allOf(node.children.collect { childNode -> addNodeAndChildren(childNode) })
			}
	}

	/**
	 * Clear this scene's existing elements.  Shorthand for
	 * {@code scene.root.clear()}.
	 */
	void clear() {

		time('Clearing scene', logger) { ->
			root.clear()
		}
	}

	/**
	 * Sort the node into one of many partitioning data structures to make future
	 * operations more efficient.
	 */
	private void partition(Node node) {

		if (node instanceof Camera) {
			camera = node
			updateableNodes << node
			return
		}

		if (node instanceof Listener) {
			listener = node
			return
		}

		var zValue = node.globalPosition.z()
		switch (node.partitionHint) {
			case PartitionHint.SMALL_AREA -> {
				var quadTree = quadTrees[zValue]
				if (!quadTree) {
					quadTree = createQuadTreeSemaphore.acquireAndRelease { ->
						return quadTrees.getOrCreate(zValue) { ->
							// TODO: Quadtree of map size, somehow find a way to pass this value in
							return new QuadTree(new Rectanglef(-1536, -1536, 1536, 1536))
						}
					}
				}
				quadTree.add(node)
			}
			case PartitionHint.LARGE_AREA, PartitionHint.NONE -> {
				var nodeList = nodeLists[zValue]
				if (!nodeList) {
					nodeList = createNodeListSemaphore.acquireAndRelease { ->
						return nodeLists.getOrCreate(zValue) { ->
							return new CopyOnWriteArrayList<Node>()
						}
					}
				}
				nodeList.add(node)
			}
		}
		zValues << zValue

		if (node instanceof GameObject) {
			updateableNodes << node
		}
		if (node instanceof InputHandler) {
			inputNodes << node
		}
	}

	/**
	 * Return all nodes that are within the given view frustum.
	 */
	List<Node> query(FrustumIntersection frustumIntersection, List<Node> results = []) {

		zValues.each { zValue ->
			nodeLists[zValue]?.each { node ->
				if (node.isVisible(frustumIntersection)) {
					results << node
				}
			}
			quadTrees[zValue]?.query(frustumIntersection, results)
		}
		return results
	}

	/**
	 * Return all nodes that are within the given range.
	 */
	List<Node> query(Circlef range, List<Node> results = []) {

		zValues.each { zValue ->
			nodeLists[zValue]?.each { node ->
				if (Intersectionf.testPointCircle(node.position.x(), node.position.y(), range.x, range.y, range.r)) {
					results << node
				}
			}
			quadTrees[zValue]?.query(range, results)
		}
		return results
	}

	/**
	 * Return all nodes of the given class.
	 */
	<T> List<T> query(Class<T> clazz, List<T> results = []) {

		if (clazz === GameObject) {
			results.addAll(updateableNodes)
		}
		else if (clazz === InputHandler) {
			results.addAll(inputNodes)
		}
		else {
			traverseAsync { node ->
				if (node.class === clazz) {
					results.add(node)
				}
				return true
			}
		}
		return results
	}

	/**
	 * Trigger the {@code onSceneRemoved} handler for this node and all its
	 * children.  Each node triggers a {@link NodeRemovedEvent} event.
	 */
	@PackageScope
	CompletableFuture<Void> removeNodeAndChildren(Node node) {

		return CompletableFuture.allOf(
			node.onSceneRemovedAsync(this),
			node.script?.onSceneRemovedAsync(this) ?: CompletableFuture.completedFuture(null)
		)
			.thenRunAsync { ->
				unpartition(node)
				trigger(new NodeRemovedEvent(node))
			}
			.thenComposeAsync { _ ->
				return CompletableFuture.allOf(node.children.collect { childNode -> removeNodeAndChildren(childNode) })
			}
	}

	/**
	 * Remove a node from the partitioning data structures.
	 */
	private void unpartition(Node node) {

		if (!quadTrees.any { zValue, quadTree -> quadTree.remove(node) }) {
			nodeLists.any { zValue, nodeList -> nodeList.remove(node) }
		}

		if (node instanceof GameObject) {
			updateableNodes.remove(node)
		}
		if (node instanceof InputHandler) {
			inputNodes.remove(node)
		}
	}

	/**
	 * A special instance of {@link Node} that is always present in the scene.
	 */
	@TupleConstructor(defaults = false)
	private static class RootNode extends Node<RootNode> {

		final Scene scene

		@Override
		String getName() {

			return 'Root node'
		}

		@Override
		protected Scene getScene() {

			return scene
		}
	}
}
