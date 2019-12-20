
// ===========================================
// Scanner's Java - Pipe/Stream implementation
// ===========================================

package redhorizon.utilities;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Wrapper of a <tt>BlockingQueue</tt> type of Java's, this class provides
 * several methods so that the underlying queue can be used as the
 * inputs/outputs of streaming file data.
 * <p>
 * The stream can be prematurely terminated by calling {@link #stopStream()}.
 * Otherwise, it will continue until the special end-of-stream packet written by
 * {@link DataSink#finish()} has been picked-up by the reader end.
 * 
 * @author Emanuel Rabina
 */
public class DataStream {

	// Number of chunks to hold in the stream before blocking
	private static final int STREAM_BUFFER_SIZE = 10;

	private final BlockingQueue<DataStreamPacket> streambuffer;
	private final DataSink sink;
	private final DataSource source;

	private Runnable stream;
	private Thread thread;

	/**
	 * Constructor, opens a new blocking data stream.
	 */
	public DataStream() {

		streambuffer = new LinkedBlockingQueue<DataStreamPacket>(STREAM_BUFFER_SIZE);
		sink    = new DataSink(this, streambuffer);
		source  = new DataSource(this, streambuffer);
	}

	/**
	 * Returns whether or not the stream has been started, and is currently
	 * streaming.
	 * 
	 * @return <tt>true</tt> if the stream has been started and is now
	 * 		   streaming, <tt>false</tt> if it hasn't.
	 */
	public boolean isStreaming() {

		return thread != null && thread.isAlive();
	}

	/**
	 * Sets the streaming thread to use the given <tt>Runnable</tt>
	 * implementation.
	 * 
	 * @param stream <tt>Runnable</tt> whose <tt>run()</tt> method makes use of
	 * 				 the stream.
	 */
	public void setStream(Runnable stream) {

		this.stream = stream;
	}

	/**
	 * Returns the sink/input end of this stream.
	 * 
	 * @return The <tt>DataStreamSink</tt> for this stream.
	 */
	public DataSink sink() {

		return sink;
	}

	/**
	 * Returns the source/output end of this stream.
	 * 
	 * @return The <tt>DataStreamSource</tt> for this stream.
	 */
	public DataSource source() {

		return source;
	}

	/**
	 * Indicates to the stream to start the <tt>Thread</tt> of the streaming
	 * algorithm registered in this class.
	 * <p>
	 * The algorithm is defined in the argument passed to the
	 * {@link #setStream(Runnable)} method.
	 */
	public void startStream() {

		thread = new Thread(stream);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	/**
	 * Closes both ends of the pipe.  Readers and writers on either end will be
	 * notified by a <tt>false</tt> return on the read/write methods of the
	 * pipe.
	 */
	public void stopStream() {

		thread = null;
	}
}
