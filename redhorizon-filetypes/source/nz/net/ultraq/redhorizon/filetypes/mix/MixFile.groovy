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

import nz.net.ultraq.redhorizon.filetypes.ArchiveFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.io.NativeRandomAccessFile

import java.util.concurrent.Semaphore

/**
 * Implementation of a MIX file.  The MIX format is a file package, much like a
 * ZIP file.
 * <p>
 * Credit goes to Olaf van der Spek and Vladan Bato for their descriptions of
 * the MIX file and some sample code, which I have adapted below.  See:
 * <ul>
 *   <li>http://xhp.xwis.net/documents/MIX_Format.html</li>
 *   <li>http://vladan.bato.net/cnc/ccfiles4.txt</li>
 * </ul>
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('mix')
class MixFile implements ArchiveFile<MixEntry> {

	private static final short FLAG_CHECKSUM  = 0x0001
	private static final short FLAG_ENCRYPTED = 0x0002

	private final NativeRandomAccessFile input
	private final Semaphore inputSemaphore = new Semaphore(1, true)

	@Delegate
	private final MixFileDelegate delegate

	/**
	 * Constructor, open a MIX file for the given File object.
	 * 
	 * @param file
	 */
	MixFile(File file) {

		input = new NativeRandomAccessFile(file)

		// Find out if this file has a checksum/encryption
		def bufferBits = input.readShort()
		def flag = input.readShort()
		if ((bufferBits == 0) && (flag & FLAG_ENCRYPTED)) {
			delegate = new MixFileDelegateEncrypted(input)
		}
		else {
			input.seek(0)
			delegate = new MixFileDelegateStandard(input)
		}
	}

	/**
	 * Calculates an ID for a {@link MixEntry} given the original file name
	 * for the entry to which it is referring to.
	 * 
	 * @param filename The original filename of the item in the MIX body.
	 * @return The ID of the entry from the filename.
	 */
	private static int calculateId(String filename) {

		def name = filename.toUpperCase()
		def id = 0

		for (def i = 0; i < name.length(); ) {
			def a = 0
			for (def j = 0; j < 4; j++) {
				a >>>= 8
				if (i < name.length()) {
					a += (short)name.charAt(i) << 24
				}
				i++
			}
			id = (id << 1 | id >>> 31) + a
		}
		return id
	}

	/**
	 * Closes the MIX file.  This will also close any input streams returned by
	 * the {@link #getEntryData} method.
	 */
	@Override
	void close() {

		input.close()
	}

	/**
	 * Returns an entry which describes an item's place in the MIX file.
	 * 
	 * @param name Name of the item and the record.
	 * @return Entry for the item, or {@code null} if the item doesn't exist in
	 *         the file.
	 */
	@Override
	MixEntry getEntry(String name) {

		def itemId = calculateId(name)
		def entry = entries.find { entry -> entry.id == itemId }
		if (entry) {
			entry.name = name
			return entry
		}
		return null
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	BufferedInputStream getEntryData(MixEntry entry) {

		return new BufferedInputStream(new MixEntryInputStream(input, inputSemaphore, baseEntryOffset, entry))
	}
}
