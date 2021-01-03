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

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * Extensions to classes to be able to read resources more easily.
 * 
 * @author Emanuel Rabina
 */
class ResourceExtensions {

	/**
	 * A shortcut to {@code ClassLoader.getResourceAsStream}.
	 * 
	 * @param self
	 * @param resourcePath
	 * @return
	 */
	static InputStream getResourceAsStream(Object self, String resourcePath) {

		def inputStream = self.class.classLoader.getResourceAsStream(resourcePath)
		if (inputStream) {
			return inputStream
		}
		throw new IllegalArgumentException("Resource not found: ${resourcePath}")
	}

	/**
	 * Wrap an input stream with a buffered one and invoke the usual
	 * {@code withStream} method over it.
	 * 
	 * @param stream
	 * @param closure
	 * @return
	 */
	static <T> T withBufferedStream(InputStream stream,
		@ClosureParams(value = SimpleType, options = 'java.io.BufferedInputStream') Closure<T> closure) {

		return new BufferedInputStream(stream).withStream(closure)
	}
}
