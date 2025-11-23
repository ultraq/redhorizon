/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.utilities

import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SpriteSheet

import groovy.transform.TupleConstructor

/**
 * Class for holding closeable resources so they can be closed in one go (if
 * they aren't closed prior).
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ResourceManager implements AutoCloseable {

	final String pathPrefix
	private final List<AutoCloseable> resources = []

	@Override
	void close() {

		resources*.close()
	}

	/**
	 * Load an image asset from an image file.
	 */
	Image loadImage(String path) {

		var image = getResourceAsStream(path).withBufferedStream { stream ->
			return new Image(path, stream)
		}
		resources << image
		return image
	}

	/**
	 * Load a sprite sheet from an image file.
	 */
	SpriteSheet loadSpriteSheet(String path) {

		var spriteSheet = getResourceAsStream(pathPrefix + path).withBufferedStream { stream ->
			return new SpriteSheet(path, stream)
		}
		resources << spriteSheet
		return spriteSheet
	}

	/**
	 * Load a palette from a palette file.
	 */
	Palette loadPalette(String path) {

		var palette = getResourceAsStream(pathPrefix + path).withBufferedStream { stream ->
			return new Palette(path, stream)
		}
		resources << palette
		return palette
	}
}
