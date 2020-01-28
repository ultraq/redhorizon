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

package nz.net.ultraq.redhorizon.classic.extensions

/**
 * Extensions for {@code byte} arrays.
 * 
 * @author Emanuel Rabina
 */
class ByteArrayExtensions {

	/**
	 * Return a new array of the items in this array but in reverse order.
	 * 
	 * @param self
	 * @return
	 */
	static byte[] reverse(byte[] self) {

		byte[] reversed = new byte[self.length]
		for (int i = 0; i < self.length; i++) {
			reversed[i] = self[self.length - 1 - i]
		}
		return reversed
	}
}
