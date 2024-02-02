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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.classic.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.StreamingDecoder
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.filetypes.codecs.Decoder
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.CompileStatic
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
 *   <li><a href="http://vladan.bato.net/cnc/vqa_frmt.txt" target="_top">http://vladan.bato.net/cnc/vqa_frmt.txt</a></li>
 *   <li><a href="https://multimedia.cx/VQA_INFO.TXT" target="_top">https://multimedia.cx/VQA_INFO.TXT</a></li>
 *   <li><a href="https://multimedia.cx/HC-VQA.TXT" target="_top">https://multimedia.cx/HC-VQA.TXT</a></li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
@FileExtensions('vqa')
@SuppressWarnings('GrFinalVariableAccess')
class VqaFile implements VideoFile {

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

		/* @formatter:off */
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
		/* @formatter:on */

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
			def worker = streamingDecoder
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
				def worker = streamingDecoder
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
	 * Return a decoder that can be used for streaming video.  The decoder will
	 * emit {@link StreamingFrameEvent}s for new frames and
	 * {@link StreamingSampleEvent}s for new sound samples.
	 */
	@Override
	StreamingDecoder getStreamingDecoder() {

		return new StreamingDecoder(new VqaFileDecoder())
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

	/**
	 * Decode VQA file frame and sound data and emit as {@link StreamingFrameEvent}s
	 * and {@link StreamingSampleEvent}s respectively.
	 */
	class VqaFileDecoder implements Runnable, EventTarget {

		private final LCW lcw = new LCW()
		private final Decoder audioDecoder

		// Precalculated values to aid frame decoding
		private final int blocksX
		private final int blockSize
		private final int modifier
		private final int numBlocks

		/**
		 * Constructor, create a new worker for decoding the VQA video data.
		 */
		VqaFileDecoder() {

			audioDecoder = bits == 16 ? new IMAADPCM16bit() : new WSADPCM8bit()

			blocksX = (width / blockWidth)
			blockSize = blockWidth * blockHeight
			modifier = blockHeight == 2 ? 0xf : 0xff
			numBlocks = blocksX * (height / blockHeight)
		}

		/**
		 * Decodes a frame of a video, found in a VPT* chunk.
		 *
		 * This method was split from the other one of the same name so it could be
		 * optimized for performance, utilizing {@code @CompileStatic} and mostly
		 * standard Java coding.
		 *
		 * @param data The VPT chunk data.
		 * @param codeBook Current lookup table for screen block data.
		 * @return A fully decoded frame of video.
		 */
		@CompileStatic
		private ByteBuffer decodeFrame(ByteBuffer data, ByteBuffer codeBook) {

			def frameBytes = ByteBuffer.allocateNative(width * height)

			// Decode block by block
			for (def block = 0; block < numBlocks; block++) {
				def framePointer = (block / blocksX as int) * width + (block * blockWidth)

				// Get the proper lookup value for the block
				def loByte = data.get(block) & 0xff
				def hiByte = data.get(block + numBlocks) & 0xff

				// Fill the block with 1 colour
				if (hiByte == modifier) {
					for (def i = 0; i < blockHeight; i++) {
						Arrays.fill(frameBytes.array(), framePointer, framePointer + blockWidth, (byte)loByte)
						framePointer += width
					}
				}
				// Otherwise, fill the block with the one referenced in the lookup table
				else {
					codeBook.position(((hiByte << 8) | loByte) * blockSize)
					for (def i = 0; i < blockHeight; i++) {
						codeBook.get(frameBytes.array(), framePointer, blockWidth)
						framePointer += width
					}
				}
			}
			return frameBytes.rewind()
		}

		/**
		 * Decodes a frame of video, found in a VPT* chunk.
		 *
		 * @param data The VPT chunk data.
		 * @param codeBook Current lookup table for screen block data.
		 * @param vqaPalette Current palette.
		 * @return A fully coloured frame of video.
		 */
		private ByteBuffer decodeFrame(ByteBuffer data, ByteBuffer codeBook, Palette vqaPalette) {

			return decodeFrame(data, codeBook).applyPalette(vqaPalette)
		}

		/**
		 * Decodes a chunk of sound, found in an SND* chunk.
		 *
		 * @param header The SND chunk header.
		 * @param data The SND chunk data.
		 * @return The sound data from this chunk.
		 */
		private ByteBuffer decodeSound(VqaChunkHeader header, ByteBuffer data) {

			return header.dataCompressed ?
				audioDecoder.decode(data, ByteBuffer.allocateNative(header.length << 2)) : // IMA ADPCM is always 4x the compression
				data
		}

		@Override
		void run() {

			Thread.currentThread().name = 'VqaFile :: Decoding'
			logger.debug('Decoding started')

			def codebook = null
			def codebookCompressed = false
			def partialCodebooks = []
			def vqaPalette = null

			def discardNullByte = { ->
				input.mark(1)
				def nextByte = input.read()
				if (nextByte == -1) {
					input.reset()
					return -1
				}
				if (nextByte) {
					input.reset()
				}
				return !nextByte
			}

			def decompressData = { ByteBuffer data, int decompressedSize ->
				return lcw.decode(data, ByteBuffer.allocateNative(decompressedSize))
			}

			def readChunkData = { VqaChunkHeader header, int decompressedSize = 0 ->
				def data = ByteBuffer.wrapNative(input.readNBytes(header.length))
				if (header.dataCompressed && decompressedSize) {
					return decompressData(data, decompressedSize)
				}
				return data
			}

			// Header + Offsets
			while (input.bytesRead < formLength && !Thread.interrupted()) {
				def chunkHeader = new VqaChunkHeader(input)

				switch (chunkHeader.name) {

				// Decode sound data
					case ~/SND./:
						def sample = decodeSound(chunkHeader, ByteBuffer.wrapNative(input.readNBytes(chunkHeader.length)))
						trigger(new StreamingSampleEvent(sample))
						break

						// Decode image and image-related data
					case 'VQFR':
						for (def innerBytesRead = 0; innerBytesRead < chunkHeader.length;) {
							def innerChunkHeader = new VqaChunkHeader(input)
							innerBytesRead += 8

							switch (innerChunkHeader.name) {

							// Full codebook
								case ~/CBF./:
									codebook = readChunkData(innerChunkHeader, numBlocks * blockSize)
									break

									// Partial codebook
								case ~/CBP./:
									partialCodebooks << readChunkData(innerChunkHeader)
									codebookCompressed = innerChunkHeader.dataCompressed
									break

									// Palette
								case ~/CPL./:
									vqaPalette = new VgaPalette(numColours, format, readChunkData(innerChunkHeader, numColours * format.value))
									break

									// Video data
								case ~/VPT./:
									def frame = average('Decoding frame', 1f, logger) { ->
										return decodeFrame(readChunkData(innerChunkHeader, numBlocks * 2), codebook, vqaPalette)
									}
									trigger(new StreamingFrameEvent(frame))
									break

								default:
									logger.debug('Unknown chunk "{}", skipping', innerChunkHeader.name)
									input.skip(chunkHeader.length)
							}

							innerBytesRead += innerChunkHeader.length
							if (discardNullByte()) {
								innerBytesRead++
							}
						}

						// If full, replace the old lookup table
						if (partialCodebooks.size() == cbParts) {
							def codebookData = ByteBuffer.fromBuffers(*partialCodebooks)
							codebook = codebookCompressed ? decompressData(codebookData, numBlocks * blockSize) : codebookData
							partialCodebooks.clear()
						}

						break

					default:
						logger.debug('Unknown chunk "{}", skipping', chunkHeader.name)
						input.skip(chunkHeader.length)
				}

				discardNullByte()
				Thread.sleep(25)
			}

			logger.debug('Decoding complete')
		}
	}
}
