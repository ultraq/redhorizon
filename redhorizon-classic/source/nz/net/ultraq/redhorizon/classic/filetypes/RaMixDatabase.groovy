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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.classic.io.NativeDataInputStream

/**
 * Mix files created with an old DOS tool, RA-MIXer, embedded something of a
 * local database at the end of the file, similar to what XCC Mixer does.  This
 * database could be used to show MIX file entry names and additional
 * descriptions.
 * <p>
 * This file always had an entry ID of 0x7fffffff and contained a text header
 * that could easily identify it.
 *
 * @author Emanuel Rabina
 */
class RaMixDatabase {

	final short unknown1
	final short numEntries
	final String textHeader
	final int unknown2
	final byte[] unknown3
	final Entry[] entries

	RaMixDatabase(InputStream stream) {

		var input = new NativeDataInputStream(stream)

		unknown1 = input.readShort()
		assert unknown1 == 256

		numEntries = input.readShort()

		textHeader = new String(input.readNBytes(44))
		assert textHeader == '+RA-MIXer 5.1 OR3, (C) MoehrchenSoft 1997,98'

		input.readBytes(80)
		unknown2 = input.readInt()
		input.readBytes(127)
		unknown3 = input.readBytes(5)

		entries = new Entry[numEntries]
		numEntries.times { index ->
			entries[index] = new Entry(
				input.readInt(),
				input.readByte(),
				new String(input.readBytes(51)).trim(),
				new String(input.readBytes(12)).trim().toLowerCase()
			)
		}
	}

	/**
	 * An entry in the RA MIX database, contains metadata about the entry in a mix
	 * file with the matching ID.
	 */
	static record Entry(int id, byte space, String description, String name) {}
}
