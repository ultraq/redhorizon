
// ===============================================
// Scanner's Java - Output end of a streaming pipe
// ===============================================

package redhorizon.utilities;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/**
 * The output end of a data stream, from which data can be read out of, once
 * something is available to be read.
 * 
 * @author Emanuel Rabina
 */
public class DataSource {

	private final DataStream stream;
	private final BlockingQueue<DataStreamPacket> source;
	private boolean finish = false;

	/**
	 * Package-only constructor, creates a new output stream using the given
	 * <tt>BlockingQueue</tt> implementation.
	 * 
	 * @param stream Parent co-ordinator.
	 * @param source The output end of a pipe.
	 */
	DataSource(DataStream stream, BlockingQueue<DataStreamPacket> source) {

		this.stream = stream;
		this.source = source;
	}

	/**
	 * Reads from the stream, the next data array, returning it into the given
	 * array of <tt>ByteBuffer</tt>s.  This method will block until the entire
	 * array can be filled, or the stream has been closed.
	 * 
	 * @param data Empty <tt>ByteBuffer</tt>[] to hold the incoming data.
	 * @return <tt>true</tt> if all of the buffers in the array were allocated,
	 * 		   <tt>false</tt> if the data couldn't be read due to the closing of
	 * 		   the pipe.
	 * @see DataSink#finish()
	 */
	public boolean read(ByteBuffer... data) {

		// End-of-stream encountered?
		if (finish) {
			return false;
		}

		// Fill available buffers until all full, end-of-stream encountered, or stream halted
		for (int i = 0; i < data.length; i++) {
			if (!stream.isStreaming()) {
				return false;
			}
			DataStreamPacket packet = source.take();
			if (packet.data == null) {
				finish = true;
				stream.stopStream();
				return false;
			}
			data[i] = packet.data;
		}
		return true;
	}
}
