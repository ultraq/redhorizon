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

package nz.net.ultraq.redhorizon.assets

import nz.net.ultraq.redhorizon.scene.Resource

/**
 * Class for holding closeable assets so they can be closed in one go (if
 * they aren't closed prior).
 *
 * @author Emanuel Rabina
 */
class AssetManager implements Resource, AutoCloseable {

	private final List<AssetResolver> resourceResolvers = []
	private final List<AutoCloseable> resources = []

	/**
	 * Add a location to search for assets.  Any supported archive files in the
	 * path are also loaded as a resource location.
	 */
	AssetManager addDirectory(String path) {

		return addResourceResolver(new FileSystemAssetResolver(path))
	}

	/**
	 * Shorthand for adding a classpath resource resolver.
	 */
	AssetManager addClasspath(String pathPrefix) {

		return addResourceResolver(new ClasspathAssetResolver(pathPrefix))
	}

	/**
	 * Add a resource resolver which can be used for locating assets using this
	 * manager.
	 */
	AssetManager addResourceResolver(AssetResolver resourceResolver) {

		resourceResolvers << resourceResolver
		return this
	}

	@Override
	void close() {

		resources*.close()
	}

	/**
	 * Load a raw input stream for the given file.  Unlike the other load methods
	 * in this class, the returned stream is the responsibility of the caller, and
	 * should be closed once used.
	 */
	BufferedInputStream loadFile(String path) {

		return new BufferedInputStream(resolveStream(path))
	}

	/**
	 * Add a resource that was loaded outside of this resource manager, so that it
	 * can be closed when this resource manager is closed.
	 */
	<T extends AutoCloseable> T manageResource(T resource) {

		resources << resource
		return resource
	}

	/**
	 * Search through all registered resource resolvers for a file with the given
	 * name.
	 */
	private InputStream resolveStream(String path) {

		var stream = resourceResolvers.findResult { resourceResolver ->
			return resourceResolver.resolve(path)
		}
		if (stream) {
			return stream
		}

		throw new FileNotFoundException("Resource not found: $path")
	}
}
