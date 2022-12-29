/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.media

import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

/**
 * An object that can be used to create media types from media files, and attach
 * them to existing game engines.
 * 
 * @param <F> The type of file to load.
 * @param <M> The type of media loaded from the file.
 * @author Emanuel Rabina
 */
abstract class MediaLoader<F, M> {

	protected final F file
	protected final Scene scene
	protected M media

	/**
	 * Constructor, create a new loader for the given media file.
	 * 
	 * @param file
	 * @param scene
	 */
	protected MediaLoader(F file, Scene scene) {

		this.file = file
		this.scene = scene
	}

	/**
	 * Load the media file into an existing scene.
	 */
	abstract M load()

	/**
	 * Unload the current media object from the scene.
	 */
	abstract void unload()
}
