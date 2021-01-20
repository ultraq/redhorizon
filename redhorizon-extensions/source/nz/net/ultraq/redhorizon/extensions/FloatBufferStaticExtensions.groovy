/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Static method extensions to the {@code FloatBuffer} class.
 * 
 * @author Emanuel Rabina
 */
class FloatBufferStaticExtensions {

	/**
	 * Allocates a direct {@code FloatBuffer} with native byte ordering.
	 * 
	 * @param self
	 * @param capacity
	 * @return Natively ordered {@code FloatBuffer}.
	 */
	static FloatBuffer allocateDirectNative(FloatBuffer self, int capacity) {

		return ByteBuffer
			.allocateDirect(capacity << 2)
			.order(ByteOrder.nativeOrder())
			.asFloatBuffer()
	}
}
