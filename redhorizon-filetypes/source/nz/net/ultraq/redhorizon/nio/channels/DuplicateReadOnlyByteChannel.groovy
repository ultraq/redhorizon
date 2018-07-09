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

package nz.net.ultraq.redhorizon.nio.channels

import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.channels.SeekableByteChannel

/**
 * A class similar to what the {@link ByteBuffer#duplicate()} method does in
 * that this class creates a new {@link SeekableByteChannel} instance over an
 * existing {@link SeekableByteChannel} so that it shares the same data but
 * maintains its own position and limit, so as not to mess with the original
 * byte channel's position/limit.  This duplicate is read-only however.
 * <p>
 * While closing this channel has no effect, closing the original will close
 * this duplicate.
 * 
 * @author Emanuel Rabina
 */
class DuplicateReadOnlyByteChannel implements SeekableByteChannel {

	private final SeekableByteChannel bytechannel
	private long position
	private boolean closed

	/**
	 * Constructor, builds a seekable byte channel over an existing one.
	 * 
	 * @param bytechannel
	 */
	DuplicateReadOnlyByteChannel(SeekableByteChannel bytechannel) {

		this.bytechannel = bytechannel
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final void close() {

		closed = true
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final boolean isOpen() {

		return !closed && bytechannel.isOpen()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final long position() {

		if (!isOpen()) {
			throw new ClosedChannelException()
		}
		return position
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final SeekableByteChannel position(long newposition) {

		if (newposition < 0) {
			throw new IllegalArgumentException()
		}
		if (!isOpen()) {
			throw new ClosedChannelException()
		}
		position = newposition
		return this
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int read(ByteBuffer dst) {

		return bytechannel.read(dst)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	long size() {

		return bytechannel.size()
	}

	/**
	 * Throws a {@link NonWritableChannelException}.
	 */
	@Override
	final SeekableByteChannel truncate(long size) {

		throw new NonWritableChannelException()
	}

	/**
	 * Throws a {@link NonWritableChannelException}.
	 */
	@Override
	final int write(ByteBuffer src) {

		throw new NonWritableChannelException()
	}
}
