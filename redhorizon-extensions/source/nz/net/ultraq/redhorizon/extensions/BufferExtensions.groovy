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

import java.nio.ByteBuffer

/**
 * Instance method extensions to the {@code Buffer} class.
 * 
 * @author Emanuel Rabina
 */
class BufferExtensions {

	/**
	 * Much like with pointers to arrays in C/C++, using {@code minus} will
	 * reverse the internal position of the buffer by the specified amount.
	 * 
	 * @param self
	 * @param n
	 * @return The buffer.
	 */
	static ByteBuffer minus(ByteBuffer self, int n) {

		return self.position(self.position() - n)
	}

	/**
	 * Advance the internal position of the buffer by 1.
	 * 
	 * @param self
	 * @param n
	 * @return The buffer.
	 */
	static ByteBuffer next(ByteBuffer self) {

		return self + 1
	}

	/**
	 * Much like with pointers to arrays in C/C++, using {@code plus} will advance
	 * the internal position of the buffer by the specified amount.
	 * 
	 * @param self
	 * @param n
	 * @return The buffer.
	 */
	static ByteBuffer plus(ByteBuffer self, int n) {

		return self.position(self.position() + n)
	}
}
