/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.scenegraph.SceneElement

import org.joml.FrustumIntersection

import groovy.transform.TupleConstructor
import java.util.concurrent.RecursiveTask

/**
 * A {@code ForkJoinTask} for culling non-visible objects from a scene.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ObjectCullingTask extends RecursiveTask<List<SceneElement>> {

	private static final int TASK_THRESHOLD = 200

	final FrustumIntersection frustumIntersection
	final List<SceneElement> sceneElements

	/**
	 * Cull the scene elements, returning only those visible within the given
	 * frustum.
	 */
	private List<SceneElement> cullObjects() {

		return sceneElements.findAll { sceneElement ->
			return sceneElement instanceof GraphicsElement && frustumIntersection.testPlaneXY(sceneElement.bounds)
		}
	}

	@Override
	protected List<SceneElement> compute() {

		if (sceneElements.size() > TASK_THRESHOLD) {
			def head = new ObjectCullingTask(frustumIntersection, sceneElements[0..<TASK_THRESHOLD])
			def tail = new ObjectCullingTask(frustumIntersection, sceneElements[TASK_THRESHOLD..<sceneElements.size()])
			tail.fork()
			return head.compute() + tail.join()
		}
		return cullObjects()
	}
}
