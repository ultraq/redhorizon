
// ==============================================
// Scanner's Java - Input end of a streaming pipe
// ==============================================

package redhorizon.utilities;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/**
 * The input end of a data stream, from which data can be pushed into, to be
 * read by the source end at a later time.
 * 
 * @author Emanuel Rabina
 */
public class DataSink {

	private final DataStream stream;
	private final BlockingQueue<DataStreamPacket> sink;
	private boolean finish = false;

	/**
	 * Package-only constructor, creates a new input stream using the given
	 * <tt>BlockingQueue</tt> implementation, using it's 'add' methods for
	 * writing.
	 * 
	 * @param stream Parent co-ordinator.
	 * @param sink	 The input end of a pipe.
	 */
	DataSink(DataStream stream, BlockingQueue<DataStreamPacket> sink) {

		this.stream = stream;
		this.sink   = sink;
	}

	/**
	 * Indicates that there is no more data to write.  This indication will be
	 * eventually picked-up by the reading end.  The stream is not considered
	 * closed until the reading end picks-up this indicator in the stream.
	 * <p>
	 * Further writes to the stream will no longer work and
	 * {@link #write(ByteBuffer...)} will then always return <tt>false</tt>.
	 */
	public void finish() {

		sink.put(new DataStreamPacket(null));
		finish = true;
	}

	/**
	 * Writes a series of <tt>ByteBuffer</tt>s to this end of the stream.  This
	 * method will block if the maximum number of buffers currently in this pipe
	 * has been reached, and will not finish until there is some room to write
	 * the data to, or the pipe has been closed.
	 * 
	 * @param data One or more <tt>ByteBuffer</tt>s to write to the stream.
	 * @return <tt>true</tt> if all buffers in <tt>sources</tt> could be
	 * 		   written, <tt>false</tt> if the stream has been closed.
	 */
	public boolean write(ByteBuffer... data) {

		// finish() called?
		if (finish) {
			return false;
		}

		// Write all data to stream, checking for stream closure along the way
		for (int i = 0; i < data.length; i++) {
			if (!stream.isStreaming()) {
				return false;
			}
			sink.put(new DataStreamPacket(data[i]));
		}
		return true;
	}
}
