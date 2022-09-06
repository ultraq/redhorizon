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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.filetypes.io.NativeRandomAccessFile

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import java.util.concurrent.Semaphore

/**
 * An {@code InputStream} around an entry in a MIX file.
 * <p>
 * Each input stream is a wrapper around the same underlying I/O channel
 * ({@code RandomAccessFile} as of writing), so we need to ensure that reads
 * between threads do not disrupt each other by splitting positioning and making
 * file accesses synchronous.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
@TupleConstructor
class MixEntryInputStream extends InputStream {

	final NativeRandomAccessFile input
	final Semaphore inputSemaphore
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
