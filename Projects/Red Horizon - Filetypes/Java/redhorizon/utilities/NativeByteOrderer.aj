/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Ensures that every {@link ByteBuffer} ever created is made with native byte
 * ordering.
 *
 * @author Emanuel Rabina
 */
public aspect NativeByteOrderer {

	/**
	 * After the construction of a byte buffer, set it to have native byte
	 * ordering.
	 * 
	 * @return ByteBuffer with native ordering.
	 */
	ByteBuffer around():
		call(public static ByteBuffer ByteBuffer.allocate*(..)) ||
		call(public static ByteBuffer ByteBuffer.wrap(..)) {

		ByteBuffer buffer = proceed();
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}
}
