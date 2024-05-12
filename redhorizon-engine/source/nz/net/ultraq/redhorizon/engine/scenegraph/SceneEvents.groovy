/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import java.util.concurrent.CompletableFuture

/**
 * Interface for all of the scene events a node will encounter during its
 * lifetime in a scene.
 *
 * @author Emanuel Rabina
 */
interface SceneEvents {

	/**
	 * Called when this node is added to the scene.
	 */
	default void onSceneAdded(Scene scene) {
	}

	/**
	 * Called when the node is removed from the scene.
	 */
	default CompletableFuture<Void> onSceneRemoved(Scene scene) {

		return CompletableFuture<Void>.completedFuture(null)
	}
}
