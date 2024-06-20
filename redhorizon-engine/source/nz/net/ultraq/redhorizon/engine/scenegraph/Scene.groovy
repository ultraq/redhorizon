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
import nz.net.ultraq.redhorizon.engine.time.TimeSystem
import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * Entry point for the Red Horizon scene graph, holds all of the objects that
 * make up the 'world'.
 *
 * @author Emanuel Rabina
 */
class Scene implements EventTarget, Visitable {

	private static final Logger logger = LoggerFactory.getLogger(Scene)

	final Node root = new RootNode(this)

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
	Listener listener
	MainMenu gameMenu
	GameWindow gameWindow

	/**
	 * Allow visitors into the scene for traversal.
	 */
	@Override
	void accept(SceneVisitor visitor) {

		root.accept(visitor)
	}

	/**
	 * Add a top-level node to this scene.  Shorthand for
	 * {@code scene.root.addNode(node)}.
	 */
	Scene addNode(Node node) {

		time('Adding node', logger) { ->
			root.addChild(node)
		}
		return this
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
	 * Locate the first node in the scene that satisfies the given predicate.
	 * Shorthand for {@code scene.root.findChild(predicate)}.
	 *
	 * @param predicate
	 * @return
	 *   The matching node, or {@code null} if no match is found.
	 */
	Node findNode(@ClosureParams(value = SimpleType, options = "Node") Closure<Boolean> predicate) {

		return root.findNode(predicate)
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
	 * Removes a top-level node from the scene.  Shorthand for
	 * {@code scene.root.removeChild(node)}.
	 *
	 * @param node
	 *   The node to remove.  If {@code null}, then this method does nothing.
	 * @return
	 *   This scene so it can be chained.
	 */
	Scene removeNode(Node node) {

		time('Removing node', logger) { ->
			root.removeChild(node)
		}
		return this
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
