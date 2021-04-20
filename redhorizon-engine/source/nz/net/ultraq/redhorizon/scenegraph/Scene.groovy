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

import nz.net.ultraq.redhorizon.engine.graphics.Camera
import nz.net.ultraq.redhorizon.engine.graphics.CameraMovedEvent

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

/**
 * Entry point for the Red Horizon scene graph, holds all of the objects that
 * make up the 'world'.
 * 
 * @author Emanuel Rabina
 */
class Scene implements Visitable {

	private static final Logger logger = LoggerFactory.getLogger(Scene)

	private final List<SceneElement> elements = new CopyOnWriteArrayList<>()
	private Camera camera
	private Matrix4f viewProjectionMatrix
	private FrustumIntersection lastFrustum

	private final ExecutorService executorService = Executors.newCachedThreadPool()
	private List<SceneElement> visibleElements = []
	private final visibleElementsLock = new Semaphore(1, true)

	/**
	 * Allow visitors into the scene for traversal.
	 * 
	 * @param visitor
	 */
	@Override
	void accept(SceneVisitor visitor) {

		elements*.accept(visitor)
	}

	/**
	 * Attach a camera to the scene, letting it be used for culling objects that
	 * aren't visible through the camera's view.
	 * 
	 * @param camera
	 */
	void addCamera(Camera camera) {

		this.camera = camera
		viewProjectionMatrix = new Matrix4f()
		lastFrustum = new FrustumIntersection(camera.projection.mul(camera.view, viewProjectionMatrix))

		// Off-thread object culling
		camera.on(CameraMovedEvent) { event ->
			executorService.execute { ->
				Thread.currentThread().name = 'Scene object culling'
				lastFrustum = new FrustumIntersection(camera.projection.mul(camera.view, viewProjectionMatrix))
				def nextVisibleElements = checkVisibility(this)
				visibleElementsLock.acquireAndRelease { ->
					visibleElements = nextVisibleElements
				}
			}
		}
	}

	/**
	 * Adds an element to this scene.
	 * 
	 * @param element
	 * @return
	 */
	Scene addSceneElement(SceneElement element) {

		elements << element
		if (camera) {
			def newVisibleElements = checkVisibility(element)
			visibleElementsLock.acquireAndRelease { ->
				visibleElements = newVisibleElements
			}
		}
		return this
	}

	/**
	 * Perform a visibility check of the given element and it's sub-elements,
	 * returning a list of those visible elements.
	 * 
	 * @param element
	 * @return
	 */
	private List<SceneElement> checkVisibility(Visitable visitable) {

		return averageNanos('objectCulling', 1f, logger) { ->
			def newVisibleElements = []
			visitable.accept { element ->
				if (lastFrustum.testPlaneXY(element.bounds)) {
					newVisibleElements << element
				}
			}
			return newVisibleElements
		}
	}

	/**
	 * Return an iterator over the currently-visible elements in the scene.
	 * 
	 * @return
	 */
	Iterator<SceneElement> getVisibleElementsIterator() {

		return visibleElementsLock.acquireAndRelease { ->
			return visibleElements.iterator()
		}
	}

	/**
	 * Overloads the {@code <<} operator to add elements to this scene.
	 * 
	 * @param element
	 * @return
	 */
	Scene leftShift(SceneElement element) {

		return addSceneElement(element)
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
