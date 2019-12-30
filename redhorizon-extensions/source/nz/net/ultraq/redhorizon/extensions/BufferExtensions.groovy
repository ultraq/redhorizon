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

import java.nio.Buffer

/**
 * Instance method extensions to the {@code Buffer} class.
 * 
 * @author Emanuel Rabina
 */
class BufferExtensions {

	/**
	 * Advance the current position of the {@code Buffer} by the given amount.
	 * Negative values can be used to move the position backwards in the buffer.
	 * 
	 * @param self
	 * @param advanceBy
	 * @return
	 */
	static Buffer advance(Buffer self, int advanceBy) {

		return self.position(self.position() + advanceBy)
	}
}
