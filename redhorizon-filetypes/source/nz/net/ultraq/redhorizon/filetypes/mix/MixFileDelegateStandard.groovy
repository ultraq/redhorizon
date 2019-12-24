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

package nz.net.ultraq.redhorizon.filetypes.mix

import groovy.transform.PackageScope

/**
 * A MIX file specific to the unencrypted format found in the original Command &
 * Conquer game.  Used as a delegate for {@link MixFile}.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
class MixFileDelegateStandard extends MixFileDelegate {

	private final int SIZE_HEADER = 6

	final short numEntries
	final int dataSize
	final MixFileEntry[] entries
	final int baseEntryOffset

	/**
	 * Constructor, start building out a MIX file using the spec from the original
	 * Command & Conquer game.
	 * 
	 * @param input
	 */
	MixFileDelegateStandard(DataInput input) {

		// File header
		numEntries = input.readShort()
		dataSize = input.readInt()

		// File entry index
		entries = new MixFileEntry[numEntries]
		numEntries.times { index ->
			entries[index] = new MixFileEntry(input)
		}

		baseEntryOffset = SIZE_HEADER + (MixFileEntry.SIZE * numEntries)
	}
}
