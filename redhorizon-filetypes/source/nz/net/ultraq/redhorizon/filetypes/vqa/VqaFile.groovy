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

package nz.net.ultraq.redhorizon.filetypes.vqa

import nz.net.ultraq.redhorizon.codecs.Decoder
import nz.net.ultraq.redhorizon.codecs.Format80
import nz.net.ultraq.redhorizon.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VgaPalette
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService

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
	final int bitrate       // Stored in file as byte
	final int unknown3
	final short unknown4
	final int maxCbfzSize
	final int unknown5

	// Frame offsets
	final String finf
	final int finfLength
	final int[] offsets

	final ColourFormat format = FORMAT_RGB

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
		bitrate      = input.readByte()
		unknown3     = input.readInt()
		unknown4     = input.readShort()
		maxCbfzSize  = input.readInt()
		unknown5     = input.readInt()

		// Chunk (frame and sound) offsets
		finf = new String(input.readNBytes(4))
		assert finf == 'FINF'
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
	ByteBuffer[] getFrameData(ExecutorService executorService) {

		def frames = []
		executorService
			.submit(getStreamingDataWorker { frame, sample ->
				if (frame) {
					frames << frame
				}
			})
			.get()
		return frames
	}

	@Override
	ByteBuffer getSoundData(ExecutorService executorService) {

		def samples = []
		executorService
			.submit(getStreamingDataWorker { frame, sample ->
				if (sample) {
					samples << sample
				}
			})
			.get()
		return ByteBuffer.fromBuffers(*samples)
	}

	/**
	 * Return a worker that can be used for streaming the video's frame and sound
	 * data to the {@code videoHandler} closure.
	 * 
	 * @param videoHandler
	 *   A closure that, when called, will be given 2 parameters: the first is
	 *   frame data, the second is sound data.  There is no guarantee that both
	 *   will be set on every call.
	 * @return Worker for streaming video data.
	 */
	@Override
	Worker getStreamingDataWorker(Closure videoHandler) {

		return new Worker() {

			private final Format80 format80 = new Format80()
			private final Decoder audioDecoder = bitrate == 16 ? new IMAADPCM16bit() : new WSADPCM8bit()

			// Precalculated values to aid frame decoding
			private final int blocksHor = width / blockWidth
			private final int blocksVer = height / blockHeight
			private final int blockSize = blockWidth * blockHeight
			private final int modifier = blockHeight == 2 ? 0xf : 0xff
			private final int nextLine = width - blockWidth
			private final int numBlocks = blocksHor * blocksVer
			private final int vptSize   = numBlocks * 2

			/**
			 * Decodes a frame of video, found in a VPT* chunk.
			 * 
			 * @param data       The VPT chunk data.
			 * @param codebook   Current lookup table for screen block data.
			 * @param vqaPalette Current palette.
			 * @return A fully decoded frame of video.
			 */
			private ByteBuffer decodeFrame(ByteBuffer data, ByteBuffer codebook, Palette vqaPalette) {

				ByteBuffer frameBytes = ByteBuffer.allocateNative(width * height)

				// Now decode every block
				int block = 0

				// Go across first, then down
				for (int y = 0; y < height; y += blockHeight) {
					for (int x = 0; x < width; x += blockWidth) {
						int framePointer = y * width + x

						// Get the proper lookup value for the block
						int loByte = data.get(block) & 0xff
						int hiByte = data.get(block + numBlocks) & 0xff

						// Fill the block with 1 colour
						if (hiByte == modifier) {
							for (int i = 1; i <= blockSize; i++) {
								frameBytes.put(framePointer++, (byte)loByte)
								if (i % blockWidth == 0) {
									framePointer += nextLine
								}
							}
						}
						// Otherwise, fill the block with the referenced block in the lookup table
						else {
							int ref = ((hiByte << 8) | loByte) * blockSize
							for (int i = 1; i <= blockSize; i++) {
								frameBytes.put(framePointer++, codebook.get(ref++))
								if (i % blockWidth == 0) {
									framePointer += nextLine
								}
							}
						}

						block++
					}
				}
				return timeWithAverage('Applying palette', 10) { ->
					return frameBytes.applyPalette(vqaPalette)
				}
			}

			/**
			 * Decodes a chunk of sound, found in an SND* chunk.
			 * 
			 * @param header The SND chunk header.
			 * @param data   The SND chunk data.
			 * @return The sound data from this chunk.
			 */
			private ByteBuffer decodeSound(VqaChunkHeader header, ByteBuffer data) {

				if (header.dataCompressed) {
					def soundBytes = ByteBuffer.allocateNative(header.length << 2) // IMA ADPCM is always 4x the compression?
					audioDecoder.decode(data, soundBytes)
					return soundBytes
				}
				return data
			}

			@Override
			void work() {

				Thread.currentThread().name = 'VqaFile :: Decoding'
				logger.debug('VqaFile decoding started')

				def codebook = null
				def codebookCompressed = false
				def partialCodebooks = []
				def vqaPalette = null

				def discardNullByte = { ->
					input.mark(1)
					def nextByte = input.read()
					if (nextByte == -1) {
						return -1
					}
					if (nextByte) {
						input.reset()
					}
					return !nextByte
				}

				def decompressData = { ByteBuffer data, int decompressedSize ->
					def decompressedData = ByteBuffer.allocateNative(decompressedSize)
					format80.decode(data, decompressedData)
					return decompressedData
				}

				def readChunkData = { VqaChunkHeader header, int decompressedSize = 0 ->
					def data = ByteBuffer.wrapNative(input.readNBytes(header.length))
					if (header.dataCompressed && decompressedSize) {
						return decompressData(data, decompressedSize)
					}
					return data
				}

				// Header + Offsets
				for (def bytesRead = 62 + finfLength; bytesRead < formLength && canContinue; ) {
					def chunkHeader = new VqaChunkHeader(input)
					bytesRead += 8

					switch (chunkHeader.name) {

						// Decode sound data
						case ~/SND./:
							videoHandler(null, decodeSound(chunkHeader, ByteBuffer.wrapNative(input.readNBytes(chunkHeader.length))))
							bytesRead += chunkHeader.length
							break

						// Decode image and image-related data
						case 'VQFR':
							for (def innerBytesRead = 0; innerBytesRead < chunkHeader.length; ) {
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
										def frame = timeWithAverage('Decoding frame', 10) { ->
											return decodeFrame(readChunkData(innerChunkHeader, vptSize), codebook, vqaPalette)
										}
										videoHandler(frame, null)
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

							bytesRead += chunkHeader.length
							break

						default:
							logger.debug('Unknown chunk "{}", skipping', chunkHeader.name)
							bytesRead += input.skip(chunkHeader.length)
					}

					if (discardNullByte()) {
						bytesRead++
					}
				}

				if (!stopped) {
					logger.debug('VqaFile decoding complete')
				}
			}
		}
	}

	/**
	 * Return some information on this VQA file.
	 * 
	 * @return VQA file info.
	 */
	@Override
	String toString() {

		return """
			VQA file, ${width}x${height} 18-bit colour with internal palette of up to 256 colours
			Contains ${numFrames} frames to run at ${String.format('%.2f', frameRate)}fps
			Sound data of ${frequency}hz ${bitrate}-bit ${channels == 2 ? 'Stereo' : 'Mono'}
		""".stripIndent().trim()
	}
}
