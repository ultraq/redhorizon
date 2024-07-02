/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

import groovy.transform.TupleConstructor
import java.nio.Buffer

/**
 * A uniform that can be shared across shader programs.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
abstract class UniformBuffer implements GraphicsResource {

	final String name

	/**
	 * Use this uniform buffer in upcoming render operations
	 */
	abstract void bind()

	/**
	 * Update a partial set of the buffer contents with the given data.
	 *
	 * @param data
	 * @param offset
	 *   The offset within the buffer.  This value should be in bytes, regardless
	 *   of the type of {@code data}.
	 */
	abstract void updateBufferData(Buffer data, int offset = 0)
}
