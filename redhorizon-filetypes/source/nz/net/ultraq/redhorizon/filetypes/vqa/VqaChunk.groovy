/* 
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes.vqa

import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import groovy.transform.PackageScope
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Representation of a "chunk" in a VQA file.  Each chunk consists of a 4-letter
 * name, the length of the data that follows in big-endian order, then that
 * data.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
class VqaChunk {

	static final String SUFFIX_UNCOMPRESSED = "0"
	static final String SUFFIX_COMPRESSED = "Z"

	final String name
	final int length
	final ByteBuffer data

	/**
	 * Constructor, takes the chunk data from the given <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing the next chunk of data.
	 */
	VqaChunk(NativeDataInputStream input) {

		name = Charset.defaultCharset().decode(ByteBuffer.wrapNative(input.readNBytes(4))).toString()
		length = Integer.reverseBytes(input.readInt())
		data = ByteBuffer.wrapNative(input.readNBytes(length))
	}

	/**
	 * Returns whether or not the chunk data is compressed.
	 * 
	 * @return
	 */
	boolean isCompressed() {

		return !name.endsWith(SUFFIX_UNCOMPRESSED)
	}
}
