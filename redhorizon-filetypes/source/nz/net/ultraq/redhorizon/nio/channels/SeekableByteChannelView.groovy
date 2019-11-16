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

package nz.net.ultraq.redhorizon.nio.channels

import groovy.transform.TupleConstructor
import java.nio.ByteBuffer
import java.nio.channels.NonWritableChannelException
import java.nio.channels.SeekableByteChannel

/**
 * A wrapper around a {@link SeekableByteChannel} to work on only a subset of
 * the source channel's overall data, maintaining its own position without
 * affecting the source channel.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false, includes = ['source', 'start', 'size'])
class SeekableByteChannelView implements SeekableByteChannel {

	private final SeekableByteChannel source
	private final long start
	private final long size
	private long position
	private boolean closed

	/**
	 * {@inheritDoc}
	 */
	@Override
	void close() {

		closed = true
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean isOpen() {

		return !closed && source.isOpen()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	long position() {

		return position
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	SeekableByteChannel position(long newPosition) {

		position = newPosition
		return this
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int read(ByteBuffer dst) {

		// Prevent the read from going beyond the upper bound of the view
		def remaining = size - position
		if (remaining == 0) {
			return -1
		}
		def oldLimit = dst.limit()
		dst.limit((dst.position() + Math.min(dst.remaining(), remaining)) as int)
		def read = source.read(dst)
		dst.limit(oldLimit)
		return read
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	long size() {

		return size
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	SeekableByteChannel truncate(long size) {

		throw new NonWritableChannelException()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int write(ByteBuffer src) {

		throw new NonWritableChannelException()
	}
}
