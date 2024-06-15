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
	 * <p>
	 * If node setup is relatively simple, override this method insted of
	 * {@link #onSceneAddedAsync}, which will wrap this one by default.
	 */
	default void onSceneAdded(Scene scene) {
	}

	/**
	 * Called when this node is added to the scene.
	 * <p>
	 * If node setup is more complicated and needs to await calls to the various
	 * sytems, override this method instead of {@link #onSceneAdded}.
	 */
	default CompletableFuture<Void> onSceneAddedAsync(Scene scene) {

		return CompletableFuture.runAsync { -> onSceneAdded(scene) }
	}

	/**
	 * Called when the node is removed from the scene.
	 * <p>
	 * If node cleanup is relatively simple, override this method insted of
	 * {@link #onSceneRemovedAsync}, which will wrap this one by default.
	 */
	default void onSceneRemoved(Scene scene) {
	}

	/**
	 * Called when the node is removed from the scene.
	 * <p>
	 * If node cleanup is more complicated and needs to await calls to the various
	 * sytems, override this method instead of {@link #onSceneRemoved}.
	 */
	default CompletableFuture<Void> onSceneRemovedAsync(Scene scene) {

		return CompletableFuture.runAsync { -> onSceneRemoved(scene) }
	}
}
