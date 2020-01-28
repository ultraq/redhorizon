/*
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.filetypes.aud

import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 * Implementation of the AUD files used in Red Alert and Tiberium Dawn.  An AUD
 * file is the sound format of choice for these games, compressed using one of 2
 * schemes: IMA-ADPCM and WS-ADPCM the latter being a Westwood proprietary
 * format.
 * <p>
 * For more information, see: http://vladan.bato.net/cnc/aud3.txt
 * 
 * @author Emanuel Rabina
 */
@FileExtensions(['aud', 'v00'])
class AudFile implements SoundFile, Streaming {

	static final byte TYPE_IMA_ADPCM = 99
	static final byte TYPE_WS_ADPCM  = 1
	static final byte FLAG_16BIT  = 0x02
	static final byte FLAG_STEREO = 0x01

	private final NativeDataInputStream input

	// File header
	final int frequency // Stored in file as short
	final int compressedSize
	final int uncompressedSize
	final byte flags
	final byte type

	final int bits
	final int channels

	/**
	 * Constructor, creates a new AUD file from the data in the input stream.
	 * 
	 * @param inputStream Input stream over an AUD file.
	 */
	AudFile(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		frequency        = input.readShort()
		compressedSize   = input.readInt()
		uncompressedSize = input.readInt()
		flags            = input.readByte()
		type             = input.readByte()
		assert type == TYPE_IMA_ADPCM || type == TYPE_WS_ADPCM

		bits = (flags & FLAG_16BIT) ? 16 : 8
		channels = (flags & FLAG_STEREO) ? 2 : 1
	}

	@Override
	ByteBuffer getSoundData() {

		def samples = []
		Executors.newSingleThreadExecutor().executeAndShutdown { executorService ->
			def worker = streamingDataWorker
			worker.on(StreamingSampleEvent) { event ->
				samples << event.sample
			}
			executorService
				.submit(worker)
				.get()
		}
		return ByteBuffer.fromBuffers(*samples)
	}

	/**
	 * Returns a worker that can be run to start streaming sound data.  The worker
	 * will emit {@link StreamingSampleEvent}s for new samples available.
	 * 
	 * @return Worker for streaming sound data.
	 */
	@Override
	Worker getStreamingDataWorker() {

		return new AudFileWorker(this, input)
	}

	/**
	 * Return a summary of this file.
	 * 
	 * @return
	 */
	@Override
	String toString() {

		return [
			"AUD file, ${frequency}hz ${bits}-bit ${channels == 2 ? 'Stereo' : 'Mono'}",
			"Encoded using ${type == TYPE_WS_ADPCM ? 'WS ADPCM' : type == TYPE_IMA_ADPCM ? 'IMA ADPCM' : '(unknown)'} algorithm",
			"Compressed: ${String.format('%,d', compressedSize)} bytes => Uncompressed: ${String.format('%,d', uncompressedSize)} bytes"
		].join(', ')
	}
}
