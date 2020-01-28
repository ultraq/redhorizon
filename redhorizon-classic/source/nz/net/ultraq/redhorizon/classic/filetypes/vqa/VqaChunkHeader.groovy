/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.filetypes.vqa

import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import groovy.transform.PackageScope

/**
 * Header for a "chunk" in a VQA file.  Each chunk header consists of a 4-letter
 * name, then the length of the data that follows in big-endian order.  Rounding
 * out the chunk will be the data afterwards.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
class VqaChunkHeader {

	static final String SUFFIX_UNCOMPRESSED = "0"
	static final String SUFFIX_COMPRESSED = "Z"

	final String name
	final int length

	/**
	 * Constructor, read a chunk header from the input stream.
	 * 
	 * @param input
	 */
	VqaChunkHeader(NativeDataInputStream input) {

		name = new String(input.readNBytes(4))
		length = Integer.reverseBytes(input.readInt())
	}

	/**
	 * Returns whether or not the chunk data that follows is compressed.
	 * 
	 * @return
	 */
	boolean isDataCompressed() {

		return !name.endsWith(SUFFIX_UNCOMPRESSED)
	}
}
