
package redhorizon.utilities.channels;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * A class similar to what the {@link ByteBuffer#duplicate()} method does in
 * that this class creates a new {@link SeekableByteChannel} instance over an
 * existing seekable channel type so that it shares the same data but maintains
 * its own position and limit, so as not to mess with the original's
 * position/limit.
 * <p>
 * While closing this channel has no effect, closing the original will close
 * this duplicate.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractDuplicateByteChannel implements SeekableByteChannel {

	protected long position;
	protected boolean closed;

	/**
	 * Create a duplicate channel.
	 */
	protected AbstractDuplicateByteChannel() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void close() {

		closed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isOpen() {

		return !closed && isOpenImpl();
	}

	/**
	 * Return whether the source is open.
	 * 
	 * @return <tt>true</tt> if the underlying source channel is open.
	 */
	protected abstract boolean isOpenImpl();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long position() {

		if (!isOpen()) {
			throw new RuntimeException(new ClosedChannelException());
		}
		return position;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final SeekableByteChannel position(long newposition) {

		if (newposition < 0) {
			throw new IllegalArgumentException();
		}
		if (!isOpen()) {
			throw new RuntimeException(new ClosedChannelException());
		}
		position = newposition;
		return this;
	}
}
