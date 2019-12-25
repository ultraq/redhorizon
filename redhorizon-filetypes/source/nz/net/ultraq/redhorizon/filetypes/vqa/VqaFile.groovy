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

import nz.net.ultraq.redhorizon.codecs.Format80
import nz.net.ultraq.redhorizon.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.utilities.ImageUtility
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB
import static nz.net.ultraq.redhorizon.filetypes.SoundFile.Bitrate.*
import static nz.net.ultraq.redhorizon.filetypes.SoundFile.Channels.*

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
	final byte channelsData
	final byte bitrateData
	final int unknown3
	final short unknown4
	final int maxCbfzSize
	final int unknown5

	final String finf
	final int finfLength
	final int[] offsets

	final Bitrate bitrate
	final Channels channels
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
		channelsData = input.readByte()
		bitrateData  = input.readByte()
		unknown3     = input.readInt()
		unknown4     = input.readShort()
		maxCbfzSize  = input.readInt()
		unknown5     = input.readInt()

		bitrate = bitrateData == 8 ? BITRATE_8 : BITRATE_16
		channels = channelsData == 1 ? CHANNELS_MONO : CHANNELS_STEREO

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
				if (frame) {
					samples << sample
				}
			})
			.get()
		return ByteBuffer.fromBuffers(samples)
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

			private static final String CHUNK_SND_TYPE_WS_ADPCM  = 'SND1'
			private static final String CHUNK_SND_TYPE_IMA_ADPCM = 'SND2'

			private final Format80 format80 = new Format80()
			private final IMAADPCM16bit imaadpcm16bit = new IMAADPCM16bit()
			private final WSADPCM8bit wsadpcm8bit = new WSADPCM8bit()

			private final int blocksHor  = width / blockWidth
			private final int blocksVer  = height / blockHeight
			private final int blockSize  = blockWidth * blockHeight
			private final int blockParts = blocksHor * blocksVer
			private final int tableSize  = maxBlocks * blockSize

			/**
			 * Decodes the complete lookup table (CBF chunk) of a VQA file.
			 * 
			 * @param data The CBF chunk data.
			 * @return A full lookup table.
			 */
			private VqaTable decodeCBFChunk(ByteBuffer data) {

				return new VqaTable(data)
			}

			/**
			 * Decodes the partial lookup table (CBP chunk) of a VQA file.
			 * 
			 * @param header        The CBP chunk header.
			 * @param data          The CBP chunk data.
			 * @param partialTables List of partial lookup tables.
			 * @return {@code true} if the partials are compressed, {@code false}
			 *   otherwise.
			 */
			private boolean decodeCBPChunk(VqaChunkHeader header, ByteBuffer data, List<ByteBuffer> partialTables) {

				partialTables << data
				return header.dataCompressed
			}

			/**
			 * Decodes the palette data (CPL chunk) of a VQA file.
			 * 
			 * @param data The CPL chunk data.
			 * @return The palette from this chunk.
			 */
			private VqaPalette decodeCPLChunk(ByteBuffer data) {

				return new VqaPalette(numColours, format, data)
			}

			/**
			 * Decodes the SND chunk of a VQA file.
			 * 
			 * @param header The SND chunk header.
			 * @param data   The SND chunk data.
			 * @param index  Last index value from any previous sound decoding.
			 * @param sample Last sample value from any previous sound decoding.
			 * @return The sound data from this chunk.
			 */
			private ByteBuffer decodeSNDChunk(VqaChunkHeader header, ByteBuffer data, byte[] index, byte[] sample) {

				def soundBytes
				if (header.dataCompressed) {
					soundBytes = ByteBuffer.allocateNative(524288) // 512K
					if (header.name == CHUNK_SND_TYPE_WS_ADPCM) {
						wsadpcm8bit.decode(data, soundBytes)
					}
					else if (header.name == CHUNK_SND_TYPE_IMA_ADPCM) {
						imaadpcm16bit.decode(data, soundBytes, ByteBuffer.wrapNative(index), ByteBuffer.wrapNative(sample))
					}
				}
				else {
					soundBytes = data
				}
				return soundBytes
			}

			/**
			 * Decodes the video data (VPT chunk) of a VQA file.
			 * 
			 * @param data       The VPT chunk data.
			 * @param vqaTable   Current lookup table.
			 * @param vqaPalette Current palette.
			 * @return A fully decoded frame of video.
			 */
			private ByteBuffer decodeVPTChunk(ByteBuffer data, VqaTable vqaTable, VqaPalette vqaPalette) {

				def frameBytes = ByteBuffer.allocateNative(width * height)

				def fillBlock = { int bx, int by, byte val ->
					for (def y = 0; y < blockHeight; y++) {
						for (def x = 0; x < blockWidth; x++) {
							frameBytes.put(((by * blockHeight) + y) * width + ((bx * blockWidth) + x), val)
						}
					}
				}

				byte modifier = blockHeight == 2 ? 0x0f : 0xff

				// Decode every block, going from top-left to bottom-right
				for (def by = 0; by < blocksVer; by++) {
					for (def bx = 0; bx < blocksHor; bx++) {
						int blockOffset = by * blocksHor + bx
						byte topVal = data.get(blockOffset)
						byte botVal = data.get((blocksHor * blocksVer) + blockOffset)

						fillBlock(bx, by, botVal == modifier ?
							topVal : // Fill the block with 1 colour
							vqaTable[((botVal & 0xff) << 8) + (topVal & 0xff)] // Fill the block with a value in the lookup table
						)
					}
				}

				// Now decode every block
//				int nextline = width - blockWidth
//				int modifier = blockHeight == 2 ? 0xf : 0xff
//				int block = 0

				// Go across first, then down
//				for (def y = 0; y < height; y += blockHeight) {
//					for (def x = 0; x < width; x += blockWidth) {
//
//						frameBytes.position(y * width + x)
//
//						// Get the proper lookup value for the block
//						int topVal = data.get(block) & 0xff
//						int botVal = data.get(block + blockParts) & 0xff
//
//						// Fill the block with 1 colour
//						if (botVal == modifier) {
//							for (def i = 1; i <= blockSize; i++) {
//								frameBytes.put((byte)topVal)
//								if ((i % blockWidth == 0) && (i != blockSize)) {
//									frameBytes.position(frameBytes.position() + nextline)
//								}
//							}
//						}
//						// Otherwise, fill the block with the value in the lookup table
//						else {
//							int ref = ((botVal << 8) + topVal) * blockSize
//							for (def i = 1; i <= blockSize; i++) {
//								frameBytes.put(vqaTable[ref++])
//								if ((i % blockWidth == 0) && (i != blockSize)) {
//									frameBytes.position(frameBytes.position() + nextline)
//								}
//							}
//						}
//
//						block++
//					}
//				}
//				frameBytes.rewind()

				return ImageUtility.applyPalette(frameBytes, vqaPalette)
			}

			/**
			 * Replaces the VQA lookup table with the complete construction of
			 * several partials.
			 * 
			 * @param partialTables List of partials so far.
			 * @param compressed    Whether or not the array of partials is compressed.
			 * @return The replacement lookup table.
			 */
			private VqaTable replaceLookupTable(List<ByteBuffer> partialTables, boolean compressed) {

				return decodeCBFChunk(
					new VqaChunkHeader(compressed ? VqaChunkHeader.SUFFIX_COMPRESSED : VqaChunkHeader.SUFFIX_UNCOMPRESSED, 0),
					ByteBuffer.fromBuffers(partialTables)
				)
			}

			@Override
			void work() {

				Thread.currentThread().name = 'VqaFile :: Decoding'
				logger.debug('VqaFile decoding started')

				def index = new byte[4]
				def sample = new byte[4]

				def partialTables = []
				def vqaTable = null
				def tablesCompressed = false
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

				def handleCompressedChunk = { VqaChunkHeader header, int decompressedSize, Closure handler ->
					def data = ByteBuffer.wrapNative(input.readNBytes(header.length))
					if (header.dataCompressed) {
						def decompressedData = ByteBuffer.allocateNative(decompressedSize)
						format80.decode(data, decompressedData)
						return handler(decompressedData)
					}
					return handler(data)
				}

				// Header + Offsets
				for (def bytesRead = 62 + finfLength; bytesRead < formLength && canContinue; ) {
					if (discardNullByte()) {
						bytesRead++
					}
					def chunkHeader = new VqaChunkHeader(input)
					bytesRead += 8

					switch (chunkHeader.name) {

						// Decode sound data
						case ~/SND./:
							videoHandler(null, decodeSNDChunk(chunkHeader, ByteBuffer.wrapNative(input.readNBytes(chunkHeader.length)), index, sample))
							bytesRead += chunkHeader.length
							break

						// Decode image and image-related data
						case 'VQFR':
							def bytesSkipped = input.skip(chunkHeader.length)
							assert bytesSkipped == chunkHeader.length
							bytesRead += bytesSkipped
							break

//							def innerBytesRead = 0
//
//							while (innerBytesRead < chunkHeader.length) {
//								if (discardNullByte()) {
//									innerBytesRead++
//								}
//								def innerChunkHeader = new VqaChunkHeader(input)
//								innerBytesRead += 8
//
//								switch (innerChunkHeader.name) {
//
//									// Full lookup table
//									case ~/CBF./:
//										vqaTable = handleCompressedChunk(innerChunkHeader, tableSize) { data ->
//											return decodeCBFChunk(data)
//										}
//										break
//
//									// Partial lookup table
//									case ~/CBP./:
//										tablesCompressed = decodeCBPChunk(innerChunkHeader,
//											ByteBuffer.wrapNative(input.readNBytes(innerChunkHeader.length)),
//											partialTables)
//										break
//
//									// Palette
//									case ~/CPL./:
//										vqaPalette = handleCompressedChunk(innerChunkHeader, blockSize) { data ->
//											return decodeCPLChunk(data)
//										}
//										break
//
//									// Video data
//									case ~/VPT./:
//										def frameData = handleCompressedChunk(innerChunkHeader, blocksHor * blocksVer * 2) { data ->
//											return decodeVPTChunk(data, vqaTable, vqaPalette)
//										}
//										videoHandler(frameData, null)
//										break
//
//									default:
//										logger.debug('Unknown chunk "{}", skipping', innerChunkHeader.name)
//										input.skip(chunkHeader.length)
//								}
//
//								innerBytesRead += innerChunkHeader.length
//							}
//
//							// If full, replace the old lookup table
//							if (partialTables.size() == cbParts) {
//								vqaTable = replaceLookupTable(partialTables, tablesCompressed)
//							}
//
//							bytesRead += innerBytesRead
//							break

						default:
							logger.debug('Unknown chunk "{}", skipping', chunkHeader.name)
							bytesRead += input.skip(chunkHeader.length)
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
			VQA file, ${width}x${height} 18-bit colour with multiple internal palettes
			Contains ${numFrames} frames to run at ${String.format('%.2f', frameRate)}fps
			Sound data of ${frequency}hz ${bitrate.value}-bit ${channels == CHANNELS_STEREO ? 'Stereo' : 'Mono'}
		""".stripIndent().trim()
	}
}
