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

package nz.net.ultraq.redhorizon.engine.resources

import nz.net.ultraq.redhorizon.audio.AudioData
import nz.net.ultraq.redhorizon.audio.StreamingAudioData
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SpriteSheet

import groovy.transform.Memoized

/**
 * Class for holding closeable resources so they can be closed in one go (if
 * they aren't closed prior).
 *
 * @author Emanuel Rabina
 */
class ResourceManager implements AutoCloseable {

	private final List<ResourceResolver> resourceResolvers = []
	private final List<AutoCloseable> resources = []

	/**
	 * Add a location to search for resources.  Any supported archive files in the
	 * path are also loaded as a resource location.
	 */
	ResourceManager addDirectory(String path) {

		return addResourceResolver(new FileSystemResourceResolver(path))
	}

	/**
	 * Shorthand for adding a classpath resource resolver.
	 */
	ResourceManager addClasspath(String pathPrefix) {

		return addResourceResolver(new ClasspathResourceResolver(pathPrefix))
	}

	/**
	 * Add a resource resolver which can be used for locating resources using this
	 * manager.
	 */
	ResourceManager addResourceResolver(ResourceResolver resourceResolver) {

		resourceResolvers << resourceResolver
		return this
	}

	@Override
	void close() {

		resources*.close()
	}

	/**
	 * Load short audio data from a file, best used for sound effects.
	 */
	@Memoized
	AudioData loadAudioData(String path) {

		var sound = resolveStream(path).withBufferedStream { stream ->
			return new AudioData(path, stream)
		}
		resources << sound
		return sound
	}

	/**
	 * Load an image asset from an image file.
	 */
	@Memoized
	Image loadImage(String path) {

		var image = resolveStream(path).withBufferedStream { stream ->
			return new Image(path, stream)
		}
		resources << image
		return image
	}

	/**
	 * Load a palette from a palette file.
	 */
	@Memoized
	Palette loadPalette(String path) {

		var palette = resolveStream(path).withBufferedStream { stream ->
			return new Palette(path, stream)
		}
		resources << palette
		return palette
	}

	/**
	 * Load a sprite sheet from an image file.
	 */
	@Memoized
	SpriteSheet loadSpriteSheet(String path) {

		var spriteSheet = resolveStream(path).withBufferedStream { stream ->
			return new SpriteSheet(path, stream)
		}
		resources << spriteSheet
		return spriteSheet
	}

	/**
	 * Load a raw input stream for the given file.
	 */
	@Memoized
	BufferedInputStream loadFile(String path) {

		var inputStream = new BufferedInputStream(resolveStream(path))
		resources << inputStream
		return inputStream
	}

	/**
	 * Load long streaming audio data from a file, best used for music tracks.
	 */
	StreamingAudioData loadStreamingAudioData(String path) {

		var musicStream = new BufferedInputStream(resolveStream(path))
		resources << musicStream

		var music = new StreamingAudioData(path, musicStream)
		resources << music

		return music
	}

	/**
	 * Search through all registered resource resolvers for a file with the given
	 * name.
	 */
	private InputStream resolveStream(String path) {

		return resourceResolvers.findResult { resourceResolver ->
			return resourceResolver.resolve(path)
		}
	}
}
