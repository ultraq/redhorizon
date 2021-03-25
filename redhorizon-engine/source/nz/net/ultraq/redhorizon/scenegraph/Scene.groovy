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

package nz.net.ultraq.redhorizon.scenegraph

import nz.net.ultraq.redhorizon.filetypes.Palette

/**
 * Entry point for the Red Horizon scene graph, holds all of the objects that
 * make up the 'world'.
 * 
 * @author Emanuel Rabina
 */
class Scene implements Visitable {

	private List<SceneElement> elements = []
	Palette palette

	/**
	 * Allow visitors into the scene for traversal.
	 * 
	 * @param visitor
	 */
	@Override
	void accept(SceneVisitor visitor) {

		elements.each { element ->
			element.accept(visitor)
		}
	}

	/**
	 * Overloads the {@code <<} operator to add elements to this scene.
	 * 
	 * @param element
	 * @return
	 */
	Scene leftShift(SceneElement element) {

		elements << element
		return this
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
	 * @param ray	  Ray to test objects against.
	 * @param node	  Node being checked for intersecting objects.
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
}
