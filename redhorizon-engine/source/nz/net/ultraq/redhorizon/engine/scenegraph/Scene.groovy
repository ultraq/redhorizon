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
import nz.net.ultraq.redhorizon.engine.graphics.Camera
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests
import nz.net.ultraq.redhorizon.engine.graphics.MainMenu
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer.GameWindow
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.time.TimeSystem
import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Entry point for the Red Horizon scene graph, holds all of the objects that
 * make up the 'world'.
 *
 * @author Emanuel Rabina
 */
class Scene implements EventTarget, Visitable {

	private static final Logger logger = LoggerFactory.getLogger(Scene)

	final List<Node> nodes = new CopyOnWriteArrayList<>()

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

	/**
	 * Allow visitors into the scene for traversal.
	 */
	@Override
	void accept(SceneVisitor visitor) {

		nodes.each { node ->
			node.accept(visitor)
		}
	}

	/**
	 * Add a top-level node to this scene.
	 */
	Scene addNode(Node node) {

		return time('Adding node', logger) { ->
			nodes << node
			addNodeAndChildren(node).join()
			return this
		}
	}

	/**
	 * Trigger the {@code onSceneAdded} event for this node and all its children.
	 * Each node triggers a {@link NodeAddedEvent} event.
	 */
	private CompletableFuture<Void> addNodeAndChildren(Node node) {

		return CompletableFuture.allOf(
			node.onSceneAdded(this),
			node.script?.onSceneAdded(this) ?: CompletableFuture.completedFuture(null)
		)
			.thenRun { ->
				trigger(new NodeAddedEvent(node))
			}
			.thenCompose { _ ->
				var futures = node.children.collect { childNode -> addNodeAndChildren(childNode) }

				// Originally used the Groovy spread operator `*` but this would throw
				// an exception about "array length is not legal" 🤷
				return CompletableFuture.allOf(futures.toArray(new CompletableFuture<Void>[0]))
			}
	}

	/**
	 * Clear this scene's existing elements.
	 */
	void clear() {

		time('Clearing scene', logger) { ->
			nodes.each { node ->
				removeNode(node)
			}
		}
	}

	/**
	 * Locate the first node in the scene that satisfies the given predicate.
	 *
	 * @param predicate
	 * @return
	 *   The matching node, or {@code null} if no match is found.
	 */
	<T extends Node> T findNode(@ClosureParams(value = SimpleType, options = "Node") Closure<Boolean> predicate) {

		return (T)nodes.find(predicate)
	}

	/**
	 * Overloads the {@code <<} operator to add elements to this scene.
	 */
	Scene leftShift(Node element) {

		return addNode(element)
	}

	/**
	 * Select objects whose bounding volumes intersect the given ray.
	 *
	 * @param ray Ray to test objects against.
	 * @return List of objects that intersect the ray.
	 */
//	public List<Spatial> pickObjects(Ray ray) {
//
//		ArrayList<Spatial> results = new ArrayList<>();
//		pickObjects(ray, rootnode, results);
//		return results;
//	}

	/**
	 * Select objects whose bounding volumes intersect the given ray.
	 *
	 * @param ray Ray to test objects against.
	 * @param node Node being checked for intersecting objects.
	 * @param results List to add intersecting objects to.
	 */
//	private void pickObjects(Ray ray, Node node, List<Spatial> results) {
//
//		for (Spatial child: node.getChildren()) {
//			if (child.intersects(ray)) {
//				if (child instanceof Node) {
//					pickObjects(ray, (Node)child, results);
//				}
//				else {
//					results.add(child);
//				}
//			}
//		}
//	}

	/**
	 * Removes a top-level node from the scene.
	 *
	 * @param node
	 *   The node to remove.  If {@code null}, then this method does nothing.
	 * @return
	 *   This scene so it can be chained.
	 */
	Scene removeNode(Node node) {

		if (node) {
			nodes.remove(node)
			removeNodeAndChildren(node).join()
		}
		return this
	}

	/**
	 * Trigger the {@code onSceneRemoved} event for this node and all its
	 * children.  Each node triggers a {@link NodeRemovedEvent} event.
	 */
	private CompletableFuture<Void> removeNodeAndChildren(Node node) {

		return CompletableFuture.allOf(
			node.onSceneRemoved(this),
			node.script?.onSceneRemoved(this) ?: CompletableFuture.completedFuture(null)
		)
			.thenRun { ->
				trigger(new NodeRemovedEvent(node))
			}
			.thenCompose { _ ->
				var futures = node.children.collect { childNode -> removeNodeAndChildren(childNode) }

				// Originally used the Groovy spread operator `*` but this would throw
				// an exception about "array length is not legal" 🤷
				return CompletableFuture.allOf(futures.toArray(new CompletableFuture<Void>[0]))
			}
	}
}
