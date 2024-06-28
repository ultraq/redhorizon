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

import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor

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

	@Delegate(includes = ['addChild', 'clear', 'findNode', 'leftShift', 'removeChild', 'traverse'], interfaces = false)
	final Node root = new RootNode(this)

	// Partition static objects in the quadTree to make culling queries faster
	final QuadTree quadTree = new QuadTree(new Rectanglef(-1536, -1536, 1536, 1536))

	Scene() {

		on(NodeAddedEvent) { event ->
			var node = event.node
			if (node instanceof Camera) {
				camera = node
			}
			else if (event.node instanceof GraphicsElement) {
				quadTree.add(event.node)
			}
		}
		on(NodeRemovedEvent) { event ->
			quadTree.remove(event.node)
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
	 * Clear this scene's existing elements.  Shorthand for
	 * {@code scene.root.clear()}.
	 */
	void clear() {

		time('Clearing scene', logger) { ->
			root.clear()
		}
	}

//	List<Node> findAll()

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

		@Override
		void traverse(SceneVisitor visitor) {

			children*.traverse(visitor)
		}
	}
}
