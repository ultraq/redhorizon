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

package nz.net.ultraq.redhorizon.filetypes.mix

import nz.net.ultraq.redhorizon.filetypes.ArchiveFileEntry

import java.nio.ByteBuffer

/**
 * Representation of a Red Alert MIX file index record, found in the header of
 * MIX files to indicate where content can be located within the body.
 * 
 * @author Emanuel Rabina
 */
class MixFileEntry implements ArchiveFileEntry, Comparable<MixFileEntry> {

	static final int SIZE = 12

	String name // Name cannot be determined initially
	final int id
	final int offset
	final int size

	/**
	 * Constructor, assigns the fields of this record from the given input source.
	 * 
	 * @param input
	 */
	MixFileEntry(DataInput input) {

		id     = input.readInt()
		offset = input.readInt()
		size   = input.readInt()
	}

	/**
	 * Constructor, assigns the fields of this record from the given byte buffer.
	 * 
	 * @param input
	 */
	MixFileEntry(ByteBuffer input) {

		id     = input.getInt()
		offset = input.getInt()
		size   = input.getInt()
	}

	/**
	 * Compares this record to the other, returns negative, zero, or positive if
	 * this record's ID is less than, equal to, or greater than the one being
	 * compared to.
	 * 
	 * @param other The other <tt>MixRecord</tt> to compare with.
	 * @return -1, 0, 1 :: less-than, equal to, greater than.
	 */
	@Override
	int compareTo(MixFileEntry other) {

		return id <=> other.id
	}
}
