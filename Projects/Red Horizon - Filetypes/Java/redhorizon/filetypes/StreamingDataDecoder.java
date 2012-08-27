
package redhorizon.filetypes;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Basic template for data decoders used by some files which chose to implement
 * streaming of their data rather than full bufferring up front.
 * 
 * @author Emanuel Rabina
 */
public abstract class StreamingDataDecoder implements Runnable {

	protected final ReadableByteChannel inputchannel;
	protected final WritableByteChannel outputchannel;

	/**
	 * Constructor, set the input and output channels for the decoder to use.
	 * 
	 * @param inputchannel
	 * @param outputchannel
	 */
	protected StreamingDataDecoder(ReadableByteChannel inputchannel, WritableByteChannel outputchannel) {

		this.inputchannel  = inputchannel;
		this.outputchannel = outputchannel;
	}

	/**
	 * Decodes the file data in a streaming manner, reading from the provided
	 * input channel and writing to the output channel.
	 */
	protected abstract void decode();

	/**
	 * Template implementation for a decoding thread.  Takes care of setting the
	 * thread name and closing the input and output channels when decoding is
	 * complete.
	 */
	@Override
	public final void run() {

		Thread.currentThread().setName(threadName());
		try {
			decode();
		}
		finally {
			inputchannel.close();
			outputchannel.close();
		}
	}

	/**
	 * Set a name to use for the thread when decoding starts.
	 * 
	 * @return Name of the decoding thread.
	 */
	protected abstract String threadName();
}
