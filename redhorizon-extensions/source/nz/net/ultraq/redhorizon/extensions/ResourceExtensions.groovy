/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.extensions

/**
 * Extensions to classes to be able to read resources more easily.
 * 
 * @author Emanuel Rabina
 */
class ResourceExtensions {

	/**
	 * A shortcut to {@code ClassLoader.getResourceAsStream} wrapped in a
	 * {@code BufferedInputStream}.
	 * 
	 * @param self
	 * @param resourcePath
	 * @return
	 */
	static InputStream getResourceAsBufferedStream(Object self, String resourcePath) {

		def inputStream = self.class.classLoader.getResourceAsStream(resourcePath)
		if (inputStream) {
			return new BufferedInputStream(inputStream)
		}
		throw new IllegalArgumentException("Resource not found: ${resourcePath}")
	}
}
