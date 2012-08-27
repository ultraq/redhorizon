
package redhorizon.utilities.channels;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * A class similar to what the {@link ByteBuffer#duplicate()} method does in
 * that this class creates a new {@link SeekableByteChannel} instance over an
 * existing {@link SeekableByteChannel} so that it shares the same data but
 * maintains its own position and limit, so as not to mess with the original
 * byte channel's position/limit.
 * <p>
 * While closing this channel has no effect, closing the original will close
 * this duplicate.
 * 
 * @author Emanuel Rabina
 */
public class DuplicateReadOnlyByteChannel extends AbstractDuplicateReadOnlyByteChannel {

	private final SeekableByteChannel bytechannel;

	/**
	 * Constructor, builds a seekable byte channel over an existing one.
	 * 
	 * @param bytechannel
	 */
	public DuplicateReadOnlyByteChannel(SeekableByteChannel bytechannel) {

		this.bytechannel = bytechannel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isOpenImpl() {

		return bytechannel.isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(ByteBuffer dst) {

		return bytechannel.read(dst);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size() {

		return bytechannel.size();
	}
}
