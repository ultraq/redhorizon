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

package nz.net.ultraq.redhorizon.classic.filetypes.shp

import nz.net.ultraq.redhorizon.io.NativeDataInputStream

/**
 * Representation of the Dune 2 SHP image header (different from the file
 * header), which contains data on the image it references.
 * 
 * @author Emanuel Rabina
 */
class ShpImageInfoDune2 {

	// The various known flags
	static final short FLAG_LOOKUP_TABLE                 = 0x01
	static final short FLAG_NO_COMPRESSION               = 0x02
	static final short FLAG_VARIABLE_LENGTH_LOOKUP_TABLE = 0x04

	// File header
	final short flags
	final byte slices
	final short width
	final byte height
	final short compressedSize
	final short uncompressedSize
	final byte[] lookupTable

	final boolean hasLookupTable
	final boolean hasVariableLengthLookupTable
	final boolean compressed

	/**
	 * Constructor, creates a Dune 2 SHP file image header.
	 * 
	 * @param input
	 */
	ShpImageInfoDune2(NativeDataInputStream input) {

		flags    = input.readShort()
		slices   = input.readByte()
		width    = input.readShort()
		height   = input.readByte()
		compressedSize = input.readShort()
		uncompressedSize = input.readShort()

		hasLookupTable = (flags & FLAG_LOOKUP_TABLE)
		hasVariableLengthLookupTable = hasLookupTable && (flags & FLAG_VARIABLE_LENGTH_LOOKUP_TABLE)
		compressed = (flags & FLAG_NO_COMPRESSION) == 0

		// Optional lookup table
		if (hasLookupTable) {
			lookupTable = hasVariableLengthLookupTable ? new byte[input.readByte() & 0xff] : new byte[16]
			lookupTable.length.times { i ->
				lookupTable[i] = input.readByte()
			}
		}
	}
}
