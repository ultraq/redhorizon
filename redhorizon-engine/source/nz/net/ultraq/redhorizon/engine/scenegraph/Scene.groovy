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
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.time.TimeSystem
import nz.net.ultraq.redhorizon.events.EventTarget

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Entry point for the Red Horizon scene graph, holds all of the objects that
 * make up the 'world'.
 *
 * @author Emanuel Rabina
 */
class Scene implements EventTarget, Visitable {

	final List<Node> nodes = new CopyOnWriteArrayList<>()

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

	/**
	 * Allow visitors into the scene for traversal.
	 *
	 * @param visitor
	 */
	@Override
	void accept(SceneVisitor visitor) {

		nodes.each { node ->
			node.accept(visitor)
		}
	}

	/**
	 * Add a node to this scene.
	 */
	Scene addNode(Node node) {

		nodes << node
		addNodeAndChildren(node)
		return this
	}

	/**
	 * Trigger the {@code onSceneAdded} event for this node and all its children.
	 * Each node triggers a {@link NodeAddedEvent} event.
	 *
	 * @param node
	 */
	private void addNodeAndChildren(Node node) {

		node.onSceneAdded(this)
		node.script?.onSceneAdded(this)
		trigger(new NodeAddedEvent(node))

		node.children.each { childNode ->
			addNodeAndChildren(childNode)
		}
	}

	/**
	 * Clear this scene's existing elements.
	 */
	void clear() {

		nodes.each { node ->
			removeNode(node)
		}
	}

	/**
	 * Locate the first node in the scene that satisfies the given predicate.
	 *
	 * @param predicate
	 * @return
	 *   The matching node, or {code null} if no node satisfies {@code predicate}.
	 */
	<T extends Node> T findNode(Closure predicate) {

		return (T)nodes.find(predicate)
	}

	/**
	 * Overloads the {@code <<} operator to add elements to this scene.
	 *
	 * @param element
	 * @return
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
	 * Removes a node from the scene.
	 *
	 * @param node
	 * @return
	 */
	Scene removeNode(Node node) {

		nodes.remove(node)
		removeNodeAndChildren(node)
		return this
	}

	/**
	 * Trigger the {@code onSceneRemoved} event for this node and all its
	 * children.  Each node triggers a {@link NodeRemovedEvent} event.
	 *
	 * @param node
	 */
	private void removeNodeAndChildren(Node node) {

		node.onSceneRemoved(this)
		node.script?.onSceneRemoved(this)
		trigger(new NodeRemovedEvent(node))

		node.children.each { childNode ->
			removeNodeAndChildren(childNode)
		}
	}
}
