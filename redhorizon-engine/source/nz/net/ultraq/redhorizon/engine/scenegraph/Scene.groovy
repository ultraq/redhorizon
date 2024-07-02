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
import nz.net.ultraq.redhorizon.engine.audio.Listener
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests
import nz.net.ultraq.redhorizon.engine.graphics.MainMenu
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer.GameWindow
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.partioning.QuadTree
import nz.net.ultraq.redhorizon.engine.time.TimeSystem
import nz.net.ultraq.redhorizon.events.EventTarget

import org.joml.FrustumIntersection
import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

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
	TimeSystem gameClock
	// TODO: A better name for this or way for nodes to have access to inputs?
	InputEventStream inputEventStream

	Window window
	Camera camera
	Listener listener
	MainMenu gameMenu
	GameWindow gameWindow

	@Delegate(includes = ['addChild', 'clear', 'findAncestor', 'findDescendent', 'leftShift', 'removeChild', 'traverse', 'traverseAsync'], interfaces = false)
	final Node root = new RootNode(this)

	// Partition objects in the following data structures to make queries faster
	private final QuadTree quadTree = new QuadTree(new Rectanglef(-1536, -1536, 1536, 1536))
	private final CopyOnWriteArrayList<Node> nodeList = []

	private final List<Node> updateableNodes = new CopyOnWriteArrayList<>()

	/**
	 * Constructor, sets up scene internals.
	 */
	Scene() {

		on(NodeAddedEvent) { event ->
			var node = event.node
			if (node instanceof Camera) {
				camera = node
				return
			}

			// Add this node to a partition if it doesn't have a renderable ancestor.
			// That ancestor will be the one to add to the partition and can take care
			// of things like the render order of descendents
			if (!node.findAncestor { parent -> parent instanceof GraphicsElement }) {
				switch (node.partitionHint) {
					case PartitionHint.SMALL_AREA -> quadTree.add(node)
					case PartitionHint.LARGE_AREA, PartitionHint.NONE -> nodeList.add(node)
				}
			}

			switch (node.updateHint) {
				case UpdateHint.ALWAYS -> updateableNodes << node
			}
		}

		on(NodeRemovedEvent) { event ->
			var node = event.node
			if (!quadTree.remove(node)) {
				nodeList.remove(node)
			}
		}
	}

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
				trigger(new NodeAddedEvent(node))
			}
			.thenComposeAsync { _ ->
				var futures = node.children.collect { childNode -> addNodeAndChildren(childNode) }

				// Originally used the Groovy spread operator `*` but this would throw
				// an exception about "array length is not legal" 🤷
				return CompletableFuture.allOf(futures.toArray(new CompletableFuture<Void>[0]))
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
	 * Return all nodes that are within the given view frustum.
	 */
	List<Node> query(FrustumIntersection frustumIntersection, List<Node> results = []) {

		nodeList.each { node ->
			if (node.isVisible(frustumIntersection)) {
				results << node
			}
		}
		quadTree.query(frustumIntersection, results)
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
				trigger(new NodeRemovedEvent(node))
			}
			.thenComposeAsync { _ ->
				var futures = node.children.collect { childNode -> removeNodeAndChildren(childNode) }

				// Originally used the Groovy spread operator `*` but this would throw
				// an exception about "array length is not legal" 🤷
				return CompletableFuture.allOf(futures.toArray(new CompletableFuture<Void>[0]))
			}
	}

	/**
	 * Run through the scene, calling {@link Node#update} as appropriate for the
	 * node configuration.
	 */
	void update(float delta) {

		var futures = updateableNodes.collect { node ->
			return CompletableFuture.runAsync { ->
				node.update(delta)
			}
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture<Void>[0])).join()
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
