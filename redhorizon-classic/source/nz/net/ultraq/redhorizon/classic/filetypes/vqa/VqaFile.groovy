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

package nz.net.ultraq.redhorizon.classic.filetypes.vqa

import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Implementation of a VQA file, which is the video format used in Red Alert and
 * Tiberium Dawn.
 * <p>
 * There are multiple documents on the VQA file format, and I've had to come up
 * with some "combination" of that information, including code from the old
 * FreeCNC project, to make this work.
 * <p>
 * See:
 * <ul>
 *   <li>http://vladan.bato.net/cnc/vqa_frmt.txt</li>
 *   <li>https://multimedia.cx/VQA_INFO.TXT</li>
 *   <li>https://multimedia.cx/HC-VQA.TXT</li>
 * </ul>
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('vqa')
@SuppressWarnings('GrFinalVariableAccess')
class VqaFile implements Streaming, VideoFile {

	private static final Logger logger = LoggerFactory.getLogger(VqaFile)

	private final NativeDataInputStream input

	// File header
	final String form
	final int formLength
	final String wvqa
	final String vqhd
	final int vqhdLength
	final short version
	final short flags
	final int numFrames     // Stored in file as short
	final int width         // Stored in file as short
	final int height        // Stored in file as short
	final byte blockWidth
	final byte blockHeight
	final float frameRate   // Stored in file as byte
	final byte cbParts
	final short numColours
	final short maxBlocks
	final int unknown1
	final short unknown2
	final int frequency     // Stored in file as short
	final int channels      // Stored in file as byte
	final int bits          // Stored in file as byte
	final int unknown3
	final short unknown4
	final int maxCbfzSize
	final int unknown5

	// Frame offsets
	final String finf
	final int finfLength
	final int[] offsets

	final ColourFormat format = FORMAT_RGB
	final boolean forVgaMonitors = true

	/**
	 * Constructor, creates a VQA file from the data in the input stream.
	 * 
	 * @param input
	 */
	VqaFile(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		form = new String(input.readNBytes(4))
		assert form == 'FORM'
		formLength = Integer.reverseBytes(input.readInt())

		wvqa = new String(input.readNBytes(4))
		assert wvqa == 'WVQA'

		vqhd = new String(input.readNBytes(4))
		assert vqhd == 'VQHD'
		vqhdLength = Integer.reverseBytes(input.readInt())

		version = input.readShort()
		assert version == 2 : 'Only C&C/RA VQAs currently supported'

		flags        = input.readShort()
		numFrames    = input.readShort()
		width        = input.readShort()
		height       = input.readShort()
		blockWidth   = input.readByte()
		blockHeight  = input.readByte()
		frameRate    = input.readByte()
		cbParts      = input.readByte()
		numColours   = input.readShort()
		maxBlocks    = input.readShort()
		unknown1     = input.readInt()
		unknown2     = input.readShort()
		frequency    = input.readShort()
		channels     = input.readByte()
		bits         = input.readByte()
		unknown3     = input.readInt()
		unknown4     = input.readShort()
		maxCbfzSize  = input.readInt()
		unknown5     = input.readInt()

		// Several unknown chunks can occur here, which we can skip for now
		while (true) {
			input.mark(4)
			def chunkName = new String(input.readNBytes(4))
			if (chunkName == 'FINF') {
				input.reset()
				break
			}
			logger.debug('Unknown chunk "{}" found in header, skipping', chunkName)
			input.skip(Integer.reverseBytes(input.readInt()))
		}

		// Chunk (frame and sound) offsets
		finf = new String(input.readNBytes(4))
		assert finf ==~ /FINF/
		finfLength = Integer.reverseBytes(input.readInt())

		offsets = new int[numFrames]
		numFrames.times { i ->
			// Multiplied by 2 because int isn't large enough!  Leads to wierdness in
			// that null bytes (0x00) are inserted between "chunks" to ensure data is
			// always starting at an even byte
			offsets[i] = (input.readInt() & 0x3fffffff) << 1
		}
	}

	@Override
	ByteBuffer[] getFrameData() {

		return Executors.newSingleThreadExecutor().executeAndShutdown { executorService ->
			def frames = []
			def worker = streamingDataWorker
			worker.on(StreamingFrameEvent) { event ->
				frames << event.frame
			}
			executorService
				.submit(worker)
				.get()
			return frames
		}
	}

	@Override
	ByteBuffer getSoundData() {

		return ByteBuffer.fromBuffers(
			Executors.newSingleThreadExecutor().executeAndShutdown { ExecutorService executorService ->
				def samples = []
				def worker = streamingDataWorker
				worker.on(StreamingSampleEvent) { event ->
					samples << event.sample
				}
				executorService
					.submit(worker)
					.get()
				return ByteBuffer.fromBuffers(samples as ByteBuffer[])
			}
		)
	}

	/**
	 * Return a worker that can be used for streaming video.  The worker will emit
	 * {@link StreamingFrameEvent}s for new frames, and
	 * {@link StreamingSampleEvent}s for new samples.
	 * 
	 * @return Worker for streaming video data.
	 */
	@Override
	Worker getStreamingDataWorker() {

		return new VqaFileWorker(this, input)
	}

	/**
	 * Return whether or not the data at the input stream could be a VQA file.
	 * 
	 * @param inputStream
	 * @return
	 */
	static boolean test(InputStream inputStream) {

		try {
			// Check file headers for certain expected values
			def input = new NativeDataInputStream(inputStream)

			def form = new String(input.readNBytes(4))
			assert form == 'FORM'
			input.readInt() // formLength

			def wvqa = new String(input.readNBytes(4))
			assert wvqa == 'WVQA'

			def vqhd = new String(input.readNBytes(4))
			assert vqhd == 'VQHD'
			input.readInt() // vqhdLength

			def version = input.readShort()
			assert version == 2

			return true
		}
		catch (Throwable ignored) {
			return false
		}
	}

	/**
	 * Return some information on this VQA file.
	 * 
	 * @return VQA file info.
	 */
	@Override
	String toString() {

		return [
			"VQA file, ${width}x${height} 18-bit w/ 256 colour palette",
			"Contains ${numFrames} frames to run at ${String.format('%.2f', frameRate)}fps",
			"Sound data of ${frequency}hz ${bits}-bit ${channels == 2 ? 'Stereo' : 'Mono'}"
		].join(', ')
	}
}
