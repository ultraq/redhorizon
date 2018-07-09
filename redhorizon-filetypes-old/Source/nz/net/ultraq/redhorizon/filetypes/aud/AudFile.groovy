/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes.aud

import nz.net.ultraq.redhorizon.filetypes.AbstractFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.SoundBitrate
import nz.net.ultraq.redhorizon.filetypes.SoundChannels
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.StreamingDataDecoder
import nz.net.ultraq.redhorizon.utilities.CodecUtility
import nz.net.ultraq.redhorizon.utilities.channels.DuplicateReadOnlyByteChannel
import static nz.net.ultraq.redhorizon.filetypes.SoundBitrate.*
import static nz.net.ultraq.redhorizon.filetypes.SoundChannels.*

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.Pipe
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SeekableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import static java.nio.file.StandardOpenOption.WRITE

/**
 * Implementation of the AUD files used in Red Alert and Tiberium Dawn.  An AUD
 * file is the sound format of choice for these games, compressed using one of 2
 * schemes: IMA-ADPCM and WS-ADPCM; the latter being a Westwood proprietary
 * format.
 * <p>
 * This is a streaming file type.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("aud")
class AudFile extends AbstractFile implements SoundFile {

	private static final byte TYPE_IMA_ADPCM = 99
	private static final byte TYPE_WS_ADPCM  = 1
	private static final byte FLAG_16BIT  = 0x02
	private static final byte FLAG_STEREO = 0x01

	private final AudFileHeader header
	private final SeekableByteChannel fileData
	private final ExecutorService decoderthreadpool = Executors.newCachedThreadPool()

	/**
	 * Constructor, creates a new aud file with the given name and data.
	 * 
	 * @param name  The name of this file.
	 * @param input Data of this aud file.
	 */
	AudFile(String name, ReadableByteChannel input) {

		super(name)

		// AUD file header
		def headerBytes = ByteBuffer.allocate(AudFileHeader.HEADER_SIZE)
		input.read(headerBytes)
		headerBytes.rewind()
		header = new AudFileHeader(headerBytes)

		// Store seekable channel types
		if (input instanceof SeekableByteChannel) {
			this.fileData = (SeekableByteChannel)input
		}

		// If the input channel isn't seekable, create a temp file that is seekable
		else {
			def tempsounddatafile = File.createTempFile(name, null)
			tempsounddatafile.deleteOnExit()
			def filechannel = FileChannel.open(Paths.get(tempsounddatafile.getAbsolutePath()), WRITE)
			filechannel.transferFrom(input, 0, header.filesize)
			this.fileData = filechannel
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {

		fileData.close()
		decoderthreadpool.shutdownNow()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SoundBitrate getBitrate() {

		return (header.flags & FLAG_16BIT) != 0 ? BITRATE_16 : BITRATE_8
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SoundChannels getChannels() {

		return (header.flags & FLAG_STEREO) != 0 ? CHANNELS_STEREO : CHANNELS_MONO
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFrequency() {

		return header.frequency & 0xffff
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getSoundData() {

		Pipe pipe = Pipe.open();
		decoderthreadpool.execute(new SoundDataDecoder(
				new DuplicateReadOnlyByteChannel(fileData), pipe.sink()));
		return pipe.source();
	}

	/**
	 * Decoder task.
	 */
	private class SoundDataDecoder extends StreamingDataDecoder {

		/**
		 * Constructor, sets the input and output for the decoding.
		 * 
		 * @param inputChannel	Channel to read the encoded input from.
		 * @param outputChannel Channel to write the decoded output to.
		 */
		private SoundDataDecoder(ReadableByteChannel inputChannel, WritableByteChannel outputChannel) {

			super(inputChannel, outputChannel);
		}

		/**
		 * Decodes the next chunk of audio data.  Assumes that the byte channel
		 * is positioned immediately after the chunk that is being passed-in.
		 * 
		 * @param chunkheader Header of the chunk to decode.
		 * @param update 2-<tt>int</tt> array, containing the latest index and
		 * 				 sample values respectively.
		 * @return Decoded sound data.
		 */
		private ByteBuffer decodeChunk(AudChunkHeader chunkheader, int[] update) {

			// Build buffers from chunk header
			ByteBuffer source = ByteBuffer.allocate(chunkheader.filesize & 0xffff);
			StreamingDataDecoder.this.inputChannel.read(source);
			source.rewind();
			ByteBuffer dest = ByteBuffer.allocate(chunkheader.datasize & 0xffff);

			// Decode
			switch (header.type) {
			case TYPE_WS_ADPCM:
				CodecUtility.decode8bitWSADPCM(source, dest);
				break;
			case TYPE_IMA_ADPCM:
				CodecUtility.decode16bitIMAADPCM(source, dest, update);
				break;
			}
			return dest;
		}

		/**
		 * Perform decoding of the sound data.
		 */
		@Override
		protected void decode() {

			ByteBuffer chunkheaderbytes = ByteBuffer.allocate(AudChunkHeader.CHUNK_HEADER_SIZE);
			int[] update = new int[2];

			// Decompress the aud file data by chunks
			while (true) {
				chunkheaderbytes.clear();
				int read = StreamingDataDecoder.this.inputChannel.read(chunkheaderbytes);
				if (read == -1) {
					break;
				}
				chunkheaderbytes.rewind();
				ByteBuffer chunkbytes = decodeChunk(new AudChunkHeader(chunkheaderbytes), update);
				StreamingDataDecoder.this.outputChannel.write(chunkbytes);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String threadName() {

			return "AudFile :: " + filename + " :: Sound data decoding thread";
		}
	}
}
