/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.audio.AudioDecoder.SampleDecodedEvent
import nz.net.ultraq.redhorizon.classic.codecs.Decoder
import nz.net.ultraq.redhorizon.classic.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.classic.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.graphics.ImageDecoder.FrameDecodedEvent
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.VideoDecoder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import java.nio.ByteBuffer
import java.text.DecimalFormat

/**
 * A video decoder for the VQA file format, which is the video format used in
 * Red Alert and Tiberium Dawn.
 *
 * <p>There are multiple documents on the VQA file format, and I've had to come
 * up with some combination of that information, including code from the old
 * FreeCNC project, to make this work.
 *
 * <p>See:
 * <ul>
 *   <li><a href="http://vladan.bato.net/cnc/vqa_frmt.txt" target="_top">http://vladan.bato.net/cnc/vqa_frmt.txt</a></li>
 *   <li><a href="https://multimedia.cx/VQA_INFO.TXT" target="_top">https://multimedia.cx/VQA_INFO.TXT</a></li>
 *   <li><a href="https://multimedia.cx/HC-VQA.TXT" target="_top">https://multimedia.cx/HC-VQA.TXT</a></li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
class VqaFileDecoder implements VideoDecoder {

	private static final Logger logger = LoggerFactory.getLogger(VqaFileDecoder)

	final String[] supportedFileExtensions = ['vqa']

	private LCW lcw
	private Decoder audioDecoder
	private int width
	private int height
	private int blockWidth
	private int blockHeight
	private int blocksX
	private int blockSize
	private int modifier
	private int numBlocks

	@Override
	DecodeSummary decode(InputStream inputStream) {

		var input = new VqaNativeDataInputStream(new NativeDataInputStream(inputStream))

		// File header
		var formHeader = input.readChunkHeader()
		assert formHeader.name() == 'FORM'
		var formLength = formHeader.length()

		var wvqa = new String(input.readNBytes(4))
		assert wvqa == 'WVQA'

		var vqhdHeader = input.readChunkHeader()
		assert vqhdHeader.name() == 'VQHD'
		var vqhdData = input.readChunkData(vqhdHeader)

		var version = vqhdData.getUnsignedShort(0)
		assert version == 2 : 'Only C&C/RA VQAs currently supported'

//		var flags = vqhdData.getShort(2)
		var numFrames = vqhdData.getUnsignedShort(4)
		width = vqhdData.getUnsignedShort(6)
		height = vqhdData.getUnsignedShort(8)
		blockWidth = vqhdData.getUnsignedByte(10)
		blockHeight = vqhdData.getUnsignedByte(11)
		var frameRate = vqhdData.getUnsignedByte(12)
		var cbParts = vqhdData.get(13)
		var numColours = vqhdData.getShort(14)
//		var maxBlocks = vqhdData.getShort(16)
//		var unknown1 = vqhdData.getInt(18)
//		var unknown2 = vqhdData.getShort(22)
		var frequency = vqhdData.getUnsignedShort(24)
		var channels = vqhdData.getUnsignedByte(26)
		var bits = vqhdData.getUnsignedByte(27)
//		var unknown3 = vqhdData.getInt(28)
//		var unknown4 = vqhdData.getShort(32)
//		var maxCbfzSize = vqhdData.getInt(34)
//		var unknown5 = vqhdData.getInt(38)

		// Several unknown chunks can occur here, which we can skip for now
		// Chunk (frame and sound) offsets, which we don't even use, so skip
		var finfHeader = input.skipUntilChunk('FINF')
		input.skipBytes(finfHeader.length())
//		var finfData = input.readChunkData(finfHeader)
//		var offsets = new int[numFrames]
//		numFrames.times { i ->
//			// Multiplied by 2 because int isn't large enough!  Leads to wierdness in
//			// that null bytes (0x00) are inserted between "chunks" to ensure data is
//			// always starting at an even byte
//			offsets[i] = (finfData.getInt() & 0x3fffffff) << 1
//		}

		trigger(new HeaderDecodedEvent(width, height, 3, numFrames, frameRate, bits, channels, frequency, 0l))

		// Precalculated values to aid frame decoding
		blocksX = (width / blockWidth) as int
		blockSize = blockWidth * blockHeight
		modifier = blockHeight == 2 ? 0xf : 0xff
		numBlocks = blocksX * (height / blockHeight) as int

		lcw = new LCW()
		audioDecoder = bits == 16 ? new IMAADPCM16bit() : new WSADPCM8bit()

		// File body
		var codebook = null
		var codebookCompressed = false
		var partialCodebooks = []
		var vqaPalette = null
		var frames = 0
		var buffers = 0

		try {
			while (input.bytesRead < formLength && !Thread.interrupted()) {
				var chunkHeader = input.readChunkHeader()

				switch (chunkHeader.name()) {
					case ~/SND./ -> {
						// Decode sound data
						var sample = decodeSound(chunkHeader, ByteBuffer.wrapNative(input.readNBytes(chunkHeader.length())))
						buffers++
						trigger(new SampleDecodedEvent(bits, channels, frequency, sample))
					}
					case 'VQFR' -> {
						// Decode image and image-related data
						for (var innerBytesRead = 0; innerBytesRead < chunkHeader.length();) {
							var innerChunkHeader = input.readChunkHeader()
							innerBytesRead += VqaChunkHeader.BYTES

							switch (innerChunkHeader.name()) {
								case ~/CBF./ -> {
									// Full codebook
									codebook = input.readChunkData(innerChunkHeader, numBlocks * blockSize)
								}
								case ~/CBP./ -> {
									// Partial codebook
									partialCodebooks << input.readChunkData(innerChunkHeader)
									codebookCompressed = innerChunkHeader.compressed
								}
								case ~/CPL./ -> {
									// Palette
									vqaPalette = new VgaPalette(numColours, 3, input.readChunkData(innerChunkHeader, numColours * 3))
								}
								case ~/VPT./ -> {
									// Video data
									var frame = average('Decoding frame', 1f, logger) { ->
										return decodeFrame(input.readChunkData(innerChunkHeader, numBlocks * 2), codebook, vqaPalette)
									}
									frames++
									trigger(new FrameDecodedEvent(width, height, 3, frame))
								}
								default -> {
									logger.debug('Unknown chunk "{}", skipping', innerChunkHeader.name())
									input.skip(chunkHeader.length())
								}
							}

							innerBytesRead += innerChunkHeader.length()
							if (input.skipNullByte()) {
								innerBytesRead++
							}
						}

						// If full, replace the old lookup table
						if (partialCodebooks.size() == cbParts) {
							var codebookData = ByteBuffer.fromBuffers(*partialCodebooks)
							codebook = codebookCompressed ?
								lcw.decode(codebookData, ByteBuffer.allocateNative(numBlocks * blockSize)) :
								codebookData
							partialCodebooks.clear()
						}
					}
					default -> {
						logger.debug('Unknown chunk "{}", skipping', chunkHeader.name())
						input.skip(chunkHeader.length())
					}
				}

				input.skipNullByte()
				Thread.yield()
			}
		}
		catch (InterruptedException ignored) {
			logger.debug('Decoding was interrupted')
		}

		return new DecodeSummary(width, height, 3, frames, bits, channels, frequency, buffers, [
			"VQA file, ${width}x${height} 18-bit w/ 256 colour palette",
			"Contains ${numFrames} frames to run at ${new DecimalFormat('0.#').format(frameRate)}fps",
			"Sound data of ${frequency}hz ${bits}-bit ${channels == 2 ? 'Stereo' : 'Mono'}"
		].join(', '))
	}

	/**
	 * Decode the "codebook", a lookup table of values used for creating a frame
	 * of video data.
	 */
	private ByteBuffer decodeCodebook(VqaChunkHeader chunkHeader, ByteBuffer data) {

		return chunkHeader.compressed ?
			lcw.decode(data, ByteBuffer.allocateNative(numBlocks * blockSize)) :
			data
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
	 * Decodes a frame of a video, found in a VPT* chunk.
	 *
	 * <p>This method was split from the other one of the same name so it could be
	 * optimized for performance, utilizing {@code @CompileStatic} and mostly
	 * standard Java coding.
	 *
	 * @param data The VPT chunk data.
	 * @param codeBook Current lookup table for screen block data.
	 * @return A fully decoded frame of video.
	 */
	@CompileStatic
	private ByteBuffer decodeFrame(ByteBuffer data, ByteBuffer codeBook) {

		var frameBytes = ByteBuffer.allocateNative(width * height)

		// Decode block by block
		for (var block = 0; block < numBlocks; block++) {
			var framePointer = (block / blocksX as int) * width + (block * blockWidth)

			// Get the proper lookup value for the block
			var loByte = data.get(block) & 0xff
			var hiByte = data.get(block + numBlocks) & 0xff

			// Fill the block with 1 colour
			if (hiByte == modifier) {
				for (var i = 0; i < blockHeight; i++) {
					Arrays.fill(frameBytes.array(), framePointer, framePointer + blockWidth, (byte)loByte)
					framePointer += width
				}
			}
			// Otherwise, fill the block with the one referenced in the lookup table
			else {
				codeBook.position(((hiByte << 8) | loByte) * blockSize)
				for (var i = 0; i < blockHeight; i++) {
					codeBook.get(frameBytes.array(), framePointer, blockWidth)
					framePointer += width
				}
			}
		}
		return frameBytes.rewind()
	}

	/**
	 * Decodes a chunk of sound, found in an SND* chunk.
	 */
	private ByteBuffer decodeSound(VqaChunkHeader header, ByteBuffer data) {

		return header.compressed ?
			audioDecoder.decode(data, ByteBuffer.allocateNative(header.length() << 2)) : // IMA ADPCM is always 4x the compression
			data
	}

	/**
	 * A wrapper around {@link NativeDataInputStream} that adds methods for
	 * working with VQA chunks.
	 */
	@TupleConstructor(defaults = false)
	private class VqaNativeDataInputStream {
		@Delegate
		final NativeDataInputStream input

		/**
		 * Reads and returns the VQA chunk data.
		 */
		ByteBuffer readChunkData(VqaChunkHeader header, int decompressedSize = 0) {
			var data = ByteBuffer.wrapNative(input.readNBytes(header.length))
			return header.compressed && decompressedSize ?
				lcw.decode(data, ByteBuffer.allocateNative(decompressedSize)) :
				data
		}

		/**
		 * Reads and returns a VQA chunk header.  A chunk header has a 4-letter
		 * name, followed by the number of bytes comprising this chunk.
		 */
		VqaChunkHeader readChunkHeader() {
			return new VqaChunkHeader(new String(input.readNBytes(4)), Integer.reverseBytes(input.readInt()))
		}

		/**
		 * If a null byte ({@code 0x00}) is next, skip over it.
		 *
		 * @return Whether a null byte was skipped.
		 */
		boolean skipNullByte() {
			input.mark(1)
			var nextByte = input.read()
			if (nextByte == -1) {
				return false
			}
			if (nextByte) {
				input.reset()
			}
			return !nextByte
		}

		/**
		 * Advance the stream until the chunk with the given name is found.
		 */
		VqaChunkHeader skipUntilChunk(String expectedName) {
			while (true) {
				var chunkHeader = readChunkHeader()
				if (chunkHeader.name() == expectedName) {
					return chunkHeader
				}
				logger.debug('Chunk "{}" encountered, but skipping until "{}" is found', chunkHeader.name(), expectedName)
				input.skip(chunkHeader.length())
			}
		}
	}

	/**
	 * Header for a "chunk" in a VQA file.  Each chunk header consists of a
	 * 4-letter name, then the length of the data that follows in big-endian
	 * order.  Rounding out the chunk will be the data afterwards.
	 */
	private static record VqaChunkHeader(String name, int length) {
		static final BYTES = 8
		static final String SUFFIX_UNCOMPRESSED = "0"
		static final String SUFFIX_COMPRESSED = "Z"

		/**
		 * Returns whether or not the chunk data that follows is compressed.
		 */
		boolean isCompressed() {
			return !name.endsWith(SUFFIX_UNCOMPRESSED)
		}
	}
}
