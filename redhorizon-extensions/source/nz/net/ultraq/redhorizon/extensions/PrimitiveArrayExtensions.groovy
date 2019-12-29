/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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
 * Extensions for primitive array types.
 * 
 * @author Emanuel Rabina
 */
class PrimitiveArrayExtensions {

	/**
	 * Fast collect method for {@code byte[]}.
	 * 
	 * @param self
	 * @param transform
	 * @return
	 */
	static byte[] collect(byte[] self, Closure transform) {

		byte[] newArray = new byte[self.length]
		for (int i = 0; i < self.length; i++) {
			newArray[i] = (byte)transform(self[i])
		}
		return newArray
	}
}
