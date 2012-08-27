
package redhorizon.utilities.channels;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;

/**
 * A read-only byte channel backed by the data in one or more byte buffers.  Any
 * changes made to the underlying buffers will be reflected here.
 * 
 * @author Emanuel Rabina
 */
public class ReadableByteChannelAdapter implements ReadableByteChannel {

	private final ByteBuffer[] buffers;
	private int currentbuffer;
	private boolean closed;

	/**
	 * Constructor, build this byte channel from one or more sources.
	 * 
	 * @param buffers
	 */
	public ReadableByteChannelAdapter(ByteBuffer... buffers) {

		if (buffers.length == 0) {
			throw new IllegalArgumentException("Must specify at least 1 buffer");
		}

		this.buffers = new ByteBuffer[buffers.length];
		for (int i = 0; i < buffers.length; i++) {
			this.buffers[i] = buffers[i].duplicate();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {

		closed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen() {

		return !closed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(ByteBuffer dst) throws ClosedChannelException {

		if (closed) {
			throw new ClosedChannelException();
		}

		// Limit the read to the size of the destination buffer or all source buffers
		int remaining = remaining();
		if (remaining == 0) {
			return -1;
		}
		int toread = Math.min(dst.remaining(), remaining);
		if (dst.remaining() > remaining) {
			dst.limit(dst.position() + toread);
		}

		// Fill the destination buffer
		for (int lefttoread = toread; lefttoread > 0 && currentbuffer < buffers.length; ) {
			ByteBuffer current = buffers[currentbuffer];
			int currentread = Math.min(current.remaining(), lefttoread);

			for (int i = 0; i < currentread; i++) {
				dst.put(current.get());
			}
			lefttoread -= currentread;

			if (current.remaining() == 0) {
				currentbuffer++;
			}
		}

		return toread;
	}

	/**
	 * Get the total number of remaining bytes across all byte arrays.
	 * 
	 * @return Number of bytes remaining.
	 */
	private int remaining() {

		if (currentbuffer < buffers.length) {
			int remaining = buffers[currentbuffer].remaining();
			for (int i = currentbuffer + 1; i < buffers.length; i++) {
				remaining += buffers[i].remaining();
			}
			return remaining;
		}
		return 0;
	}
}
