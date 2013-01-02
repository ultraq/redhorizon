/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.scenegraph;

import redhorizon.engine.audio.AudioRenderer;
import redhorizon.engine.audio.Listener;
import redhorizon.engine.graphics.Camera;
import redhorizon.geometry.Ray;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the Red Horizon scene graph, holds and manages all of the
 * {@link Node}s, which in turn hold the spatial information of all of the
 * objects currently in the 'world'.
 * 
 * @author Emanuel Rabina
 */
public class Scene {

	// Scene graph structure
	private final Node rootnode = new Node();
	private Listener listener;
	private Camera camera;

	/**
	 * Default constructor, initializes the data structures used to hold the
	 * scene nodes.
	 */
	public Scene() {
	}

	/**
	 * Returns the camera into this scene.
	 * 
	 * @return Scene camera, or <tt>null</tt> if no camera has been set.
	 */
	public Camera getCamera() {

		return camera;
	}

	/**
	 * Returns the listener into this scene.
	 * 
	 * @return Scene listener, or <tt>null</tt> if no listener has been set.
	 */
	public Listener getListener() {

		return listener;
	}

	/**
	 * Returns the root node for this scene, from which all other scene nodes
	 * are held.
	 * 
	 * @return The root scene node.
	 */
	public Node getRootNode() {

		return rootnode;
	}

	/**
	 * Select objects whose bounding volumes intersect the given ray.
	 * 
	 * @param ray Ray to test objects against.
	 * @return List of objects that intersect the ray.
	 */
	public List<Spatial> pickObjects(Ray ray) {

		ArrayList<Spatial> results = new ArrayList<>();
		pickObjects(ray, rootnode, results);
		return results;
	}

	/**
	 * Select objects whose bounding volumes intersect the given ray.
	 * 
	 * @param ray	  Ray to test objects against.
	 * @param node	  Node being checked for intersecting objects.
	 * @param results List to add intersecting objects to.
	 */
	private void pickObjects(Ray ray, Node node, List<Spatial> results) {

		for (Spatial child: node.getChildren()) {
			if (child.intersects(ray)) {
				if (child instanceof Node) {
					pickObjects(ray, (Node)child, results);
				}
				else {
					results.add(child);
				}
			}
		}
	}

	/**
	 * Renders all of the audio objects within this scene.
	 * 
	 * @param renderer
	 */
	public void render(AudioRenderer renderer) {

		listener.render(renderer);
		rootnode.render(renderer);
	}

	/**
	 * Set the camera into this scene.
	 * 
	 * @param camera
	 */
	public void setCamera(Camera camera) {

		this.camera = camera;
	}

	/**
	 * Set the listener into this scene.
	 * 
	 * @param listener
	 */
	public void setListener(Listener listener) {

		this.listener = listener;
	}
}
