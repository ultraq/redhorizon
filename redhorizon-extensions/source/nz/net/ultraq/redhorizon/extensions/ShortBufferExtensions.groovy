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

import java.nio.ShortBuffer

/**
 * Instance method extensions to the {@code ShortBuffer} class.
 * 
 * @author Emanuel Rabina
 */
class ShortBufferExtensions {

	/**
	 * Advance the internal position of a buffer by the given amount.  Negative
	 * values may be given to move the position backwards.
	 * 
	 * @param self
	 * @param n
	 * @return The buffer.
	 */
	static ShortBuffer advance(ShortBuffer self, int n) {

		return self.position(self.position() + n)
	}
}
