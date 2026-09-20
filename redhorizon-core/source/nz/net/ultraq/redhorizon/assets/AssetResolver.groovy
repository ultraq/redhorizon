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

/**
 * An interface for taking a name or path of an asset and returning a stream
 * of its contents, if the asset exists.
 *
 * @author Emanuel Rabina
 */
interface AssetResolver {

	/**
	 * Given a path to an asset, return an input stream for reading its
	 * contents, or {@code null} if the asset doesn't exist.
	 */
	InputStream resolve(String path)
}
