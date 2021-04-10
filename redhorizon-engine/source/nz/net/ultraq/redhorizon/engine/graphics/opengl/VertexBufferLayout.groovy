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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import groovy.transform.TupleConstructor

/**
 * Representation of the layout of a vertex buffer.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class VertexBufferLayout {

	final VertexBufferLayoutParts[] parts

	/**
	 * Return the offset value of the given layout part, in floats.
	 * 
	 * @param layoutPart
	 * @return
	 */
	int offsetOf(VertexBufferLayoutParts layoutPart) {

		def offset = 0
		for (def i = 0; i < parts.size(); i++) {
			def part = parts[i]
			if (part == layoutPart) {
				return offset
			}
			offset += part.size
		}
		return -1
	}

	/**
	 * Return the offset value of the given layout part, in floats.
	 * 
	 * @param layoutPart
	 * @return
	 */
	int offsetOfInBytes(VertexBufferLayoutParts layoutPart) {

		def offset = offsetOf(layoutPart)
		return offset != -1 ? offset * Float.BYTES : -1
	}

	/**
	 * Return the size of this layout in floats.
	 * 
	 * @return
	 */
	int size() {

		return parts.sum { part -> part.size } as int
	}

	/**
	 * Return the size of this layout in bytes.
	 * 
	 * @return
	 */
	int sizeInBytes() {

		return size() * Float.BYTES
	}
}
