/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics.extensions

import nz.net.ultraq.redhorizon.assets.AssetManager
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SpriteSheet

import groovy.transform.Memoized

/**
 * Adds graphics asset loading methods to the {@link AssetManager}.
 *
 * @author Emanuel Rabina
 */
class AssetManagerExtensions {

	/**
	 * Load an image asset from an image file.
	 */
	@Memoized
	static Image loadImage(AssetManager self, String path) {

		return self.loadFile(path).withCloseable { inputStream ->
			return self.manageResource(new Image(path, inputStream))
		}
	}

	/**
	 * Load a palette from a palette file.
	 */
	@Memoized
	static Palette loadPalette(AssetManager self, String path) {

		return self.loadFile(path).withCloseable { inputStream ->
			return self.manageResource(new Palette(path, inputStream))
		}
	}

	/**
	 * Load a sprite sheet from an image file.
	 */
	@Memoized
	static SpriteSheet loadSpriteSheet(AssetManager self, String path) {

		return self.loadFile(path).withCloseable { inputStream ->
			return self.manageResource(new SpriteSheet(path, inputStream))
		}
	}
}
