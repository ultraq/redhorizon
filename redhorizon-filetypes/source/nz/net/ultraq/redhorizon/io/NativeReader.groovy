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

package nz.net.ultraq.redhorizon.io

import groovy.transform.PackageScope

/**
 * Trait of IO classes that can read data in little-endian order if necessary.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
trait NativeReader {

	abstract int read()

	/**
	 * Read the given number of bytes in little endian byte order, returning the
	 * expected primitive that comprises those bytes.
	 * 
	 * @param numBytes
	 * @return
	 */
	int readLittleEndian(int numBytes) {

		def result = 0
		for (def i = 0; i < numBytes; i++) {
			def b = read()
			if (b < 0) {
				throw new EOFException()
			}
			result += b << (8 * i)
		}
		return result
	}
}
