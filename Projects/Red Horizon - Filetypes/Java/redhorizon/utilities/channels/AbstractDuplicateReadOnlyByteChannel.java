
package redhorizon.utilities.channels;

import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * Most of the duplicate channels are read-only, so this implements many of the
 * write operations to throw the {@link NonWritableChannelException}.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractDuplicateReadOnlyByteChannel extends AbstractDuplicateByteChannel {

	/**
	 * Create a duplicate read-only channel.
	 */
	protected AbstractDuplicateReadOnlyByteChannel() {
	}

	/**
	 * Throws a {@link NonWritableChannelException}.
	 */
	@Override
	public final SeekableByteChannel truncate(long size) {

		throw new NonWritableChannelException();
	}

	/**
	 * Throws a {@link NonWritableChannelException}.
	 */
	@Override
	public final int write(ByteBuffer src) {

		throw new NonWritableChannelException();
	}
}
