
package redhorizon.filetypes.mix;

import redhorizon.utilities.channels.AbstractDuplicateReadOnlyByteChannel;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Byte channel over a record in the mix file.
 * 
 * @author Emanuel Rabina
 */
public class MixRecordByteChannel extends AbstractDuplicateReadOnlyByteChannel {

	private final FileChannel filechannel;
	private final long lowerbound;
	private final long upperbound;

	/**
	 * Constructor, creates a byte channel backed by the mix file's file
	 * channel.
	 * 
	 * @param filechannel
	 * @param lowerbound
	 * @param size
	 */
	MixRecordByteChannel(FileChannel filechannel, int lowerbound, int size) {

		this.filechannel = filechannel;
		this.lowerbound  = lowerbound;
		this.upperbound  = lowerbound + size;

		position = lowerbound;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isOpenImpl() {

		return filechannel.isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(ByteBuffer dst) {

		// Prevent the read from going beyond the upper bound of the file entry
		int remaining = (int)(upperbound - position);
		if (remaining == 0) {
			return -1;
		}
		int oldlimit = dst.limit();
		dst.limit(dst.position() + Math.min(dst.remaining(), remaining));
		int read = filechannel.read(dst, position);
		dst.limit(oldlimit);
		return read;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size() {

		return upperbound - lowerbound;
	}
}
