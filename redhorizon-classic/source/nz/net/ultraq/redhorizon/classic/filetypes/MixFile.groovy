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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.filetypes.ArchiveFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.io.NativeRandomAccessFile

import groovy.transform.TupleConstructor
import java.util.concurrent.Semaphore

/**
 * Implementation of a MIX file.  The MIX format is a file package, much like a
 * ZIP file.
 * <p>
 * Credit goes to Olaf van der Spek and Vladan Bato for their descriptions of
 * the MIX file and some sample code, which I have adapted below.  See:
 * <ul>
 *   <li><a href="http://xhp.xwis.net/documents/MIX_Format.html" target="_top">http://xhp.xwis.net/documents/MIX_Format.html</a></li>
 *   <li><a href="http://vladan.bato.net/cnc/ccfiles4.txt" target="_top">http://vladan.bato.net/cnc/ccfiles4.txt</a></li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
@FileExtensions('mix')
class MixFile implements ArchiveFile<MixEntry> {

	private static final short FLAG_CHECKSUM = 0x0001
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
	static int calculateId(String filename) {

		def name = filename.toUpperCase()
		def id = 0

		for (def i = 0; i < name.length();) {
			def a = 0
			4.times { j ->
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
	 * Returns the entry with the matching ID, or {@code null} if it doesn't
	 * exist.
	 */
	MixEntry getEntry(int id) {

		return entries.find { entry -> entry.id == id }
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

	@Override
	InputStream getEntryData(MixEntry entry) {

		return new MixEntryInputStream(baseEntryOffset, entry)
	}

	/**
	 * An {@code InputStream} around an entry in a MIX file.
	 * <p>
	 * Each input stream is a wrapper around the same underlying I/O channel
	 * ({@code RandomAccessFile} as of writing), so we need to ensure that reads
	 * between threads do not disrupt each other by splitting positioning and
	 * making file accesses synchronous.
	 */
	@TupleConstructor
	class MixEntryInputStream extends InputStream {

		final int baseEntryOffset
		final MixEntry entry

		private int lastMark = -1
		private int lastPosition = 0

		@Override
		synchronized void mark(int readLimit) {

			lastMark = lastPosition
		}

		@Override
		boolean markSupported() {

			return true
		}

		@Override
		int read() {

			return inputSemaphore.acquireAndRelease { ->
				if (lastPosition >= entry.size) {
					return -1
				}
				input.seek(baseEntryOffset + entry.offset + lastPosition)
				def b = input.read()
				lastPosition++
				return b
			}
		}

		@Override
		int read(byte[] b, int off, int len) {

			return inputSemaphore.acquireAndRelease { ->
				if (lastPosition >= entry.size) {
					return -1
				}
				input.seek(baseEntryOffset + entry.offset + lastPosition)
				def toRead = Math.min(len, entry.size - lastPosition)
				def bytesRead = input.read(b, off, toRead)
				lastPosition += bytesRead
				return bytesRead
			}
		}

		@Override
		synchronized void reset() {

			lastPosition = lastMark
		}

		@Override
		long skip(long n) {

			return inputSemaphore.acquireAndRelease { ->
				def toSkip = Math.min(n, entry.size - lastPosition)
				lastPosition += toSkip
				return toSkip
			}
		}
	}
}
