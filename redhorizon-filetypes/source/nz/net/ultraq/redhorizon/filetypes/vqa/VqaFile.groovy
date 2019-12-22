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
import java.nio.charset.Charset
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
 *   <li>https://multimedia.cx/HC-VQA.TXT</li>
 *   <li>http://xhp.xwis.net/documents/vqp_info.txt</li>
 * </ul>
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('vqa')
class VqaFile implements Streaming, VideoFile {

	private static final Logger logger = LoggerFactory.getLogger(VqaFile)

	private final NativeDataInputStream input

	// File header
	final char[] signature
	final int startPos
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
	final short unknown1
	final int unknown2
	final int frequency     // Stored in file as short
	final byte channelsData
	final byte bitrateData
	final byte[] unknown3
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
		signature = Charset.defaultCharset().decode(ByteBuffer.wrapNative(input.readNBytes(8))).array()
		assert signature.toString() == 'FORM'

		startPos     = Integer.reverseBytes(input.readInt())
		version      = input.readShort()
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
		unknown1     = input.readShort()
		unknown2     = input.readInt()
		frequency    = input.readShort()
		channelsData = input.readByte()
		bitrateData  = input.readByte()
		unknown3     = input.readNBytes(14)

		bitrate = bitrateData == 8 ? BITRATE_8 : BITRATE_16
		channels = channelsData == 1 ? CHANNELS_MONO : CHANNELS_STEREO

		// Frame and sound offsets
		// Shouldn't need the null byte skip since the header is always an even number of bytes
//		readNextChunk(bytechannel)
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

			private final int blockSize  = blockWidth * blockHeight
			private final int blockParts = (width / blockWidth) * (height / blockHeight)
			private final int tableSize  = maxBlocks * blockSize

			/**
			 * Decodes the complete lookup table (CBF chunk) of a VQA file.
			 * 
			 * @param chunk The CBF chunk.
			 * @return A full lookup table.
			 */
			private VqaTable decodeCBFChunk(VqaChunk chunk) {

				def tableBytes
				if (chunk.compressed) {
					tableBytes = ByteBuffer.allocateNative(tableSize)
					format80.decode(chunk.data, tableBytes)
				}
				else {
					tableBytes = chunk.data
				}
				return new VqaTable(tableBytes)
			}

			/**
			 * Decodes the partial lookup table (CBP chunk) of a VQA file.
			 * 
			 * @param chunk The CBP chunk
			 * @param partialTables List of partial lookup tables.
			 * @return {@code true} if the partials are compressed, {@code false}
			 *   otherwise.
			 */
			private boolean decodeCBPChunk(VqaChunk chunk, List<ByteBuffer> partialTables) {

				partialTables << chunk.data
				return chunk.compressed
			}

			/**
			 * Decodes the palette data (CPL chunk) of a VQA file.
			 *
			 * @param chunk The CPL chunk.
			 * @return The palette from this chunk.
			 */
			private VqaPalette decodeCPLChunk(VqaChunk chunk) {

				def paletteBytes
				if (chunk.compressed) {
					paletteBytes = ByteBuffer.allocate(blockSize)
					format80.decode(chunk.data, paletteBytes)
				}
				else {
					paletteBytes = chunk.data
				}
				return new VqaPalette(numColours, format, paletteBytes)
			}

			/**
			 * Decodes the SND chunk of a VQA file.
			 * 
			 * @param chunk  The SND chunk.
			 * @param index  Last index value from any previous sound decoding.
			 * @param sample Last sample value from any previous sound decoding.
			 * @return The sound data from this chunk.
			 */
			private ByteBuffer decodeSNDChunk(VqaChunk chunk, byte[] index, byte[] sample) {

				def soundBytes
				if (chunk.compressed) {
					soundBytes = ByteBuffer.allocateNative(524288) // 512K
					if (chunk.name == CHUNK_SND_TYPE_WS_ADPCM) {
						wsadpcm8bit.decode(chunk.data, soundBytes)
					}
					else if (chunk.name == CHUNK_SND_TYPE_IMA_ADPCM) {
						imaadpcm16bit.decode(chunk.data, soundBytes, ByteBuffer.wrapNative(index), ByteBuffer.wrapNative(sample))
					}
				}
				else {
					soundBytes = chunk.data
				}
				return soundBytes
			}

			/**
			 * Decodes the video data (VPT chunk) of a VQA file.
			 * 
			 * @param chunk      The VPT chunk.
			 * @param vqaTable   Current lookup table.
			 * @param vqaPalette Current palette.
			 * @return A fully decoded frame of video.
			 */
			private ByteBuffer decodeVPTChunk(VqaChunk chunk, VqaTable vqaTable, VqaPalette vqaPalette) {

				def videoBytes
				if (chunk.compressed) {
					videoBytes = ByteBuffer.allocateNative(blockParts << 1)
					format80.decode(chunk.data, videoBytes)
				}
				else {
					videoBytes = chunk.data
				}

				def frameBytes = ByteBuffer.allocateNative(width * height)

				// Now decode every block
				int nextline = width - blockWidth
				byte modifier = blockHeight == 2 ? 0xf : 0xff
				int block = 0

				// Go across first, then down
				for (int y = 0; y < height; y += blockHeight) {
					for (int x = 0; x < width; x += blockWidth) {

						frameBytes.position(y * width + x)

						// Get the proper lookup value for the block
						byte topVal = videoBytes.get(block)
						byte botVal = videoBytes.get(block + blockParts)

						// Fill the block with 1 colour
						if (botVal == modifier) {
							for (int i = 1; i <= blockSize; i++) {
								frameBytes.put(topVal)
								if ((i % blockWidth == 0) && (i != blockSize)) {
									frameBytes.position(frameBytes.position() + nextline)
								}
							}
						}
						// Otherwise, fill the block with the value in the lookup table
						else {
							int ref = ((botVal << 8) + topVal) * blockSize
							for (int i = 1; i <= blockSize; i++) {
								frameBytes.put(vqaTable[ref++])
								if ((i % blockWidth == 0) && (i != blockSize)) {
									frameBytes.position(frameBytes.position() + nextline)
								}
							}
						}

						block++
					}
				}
				frameBytes.rewind()

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

				return decodeCBFChunk([
				  name: compressed ? VqaChunk.SUFFIX_COMPRESSED : VqaChunk.SUFFIX_UNCOMPRESSED,
					data: ByteBuffer.fromBuffers(partialTables)
				] as VqaChunk)
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

				for (def frame = 0; canContinue && frame < numFrames; frame++) {

					// Discard null bytes
					input.mark(1)
					def nextByte = input.readByte()
					if (nextByte) {
						input.reset()
					}
					// TODO: Might not need this branch if the for loop catches it
					else if (nextByte == -1) {
						break
					}

					// Read next chunk header
					def chunk = new VqaChunk(input)

					// Decode sound data
					if (chunk.name.startsWith('SND')) {
						def soundData = decodeSNDChunk(chunk, index, sample)
						videoHandler(null, soundData)
					}

					// Decode image and image-related data
					else if (chunk.name == 'VQFR') {
						outer: while (true) {
							def innerChunk = new VqaChunk(input)

							// Full lookup table
							if (innerChunk.name.startsWith('CBF')) {
								vqaTable = decodeCBFChunk(innerChunk)
							}

							// Partial lookup table
							else if (innerChunk.name.startsWith('CBP')) {
								tablesCompressed = decodeCBPChunk(innerChunk, partialTables)
							}

							// Palette
							else if (innerChunk.name.startsWith('CPL')) {
								vqaPalette = decodeCPLChunk(innerChunk)
							}

							// Video data
							else if (innerChunk.name.startsWith('VPT')) {
								ByteBuffer frameData = decodeVPTChunk(innerChunk, vqaTable, vqaPalette)
								videoHandler(frameData)
								break outer
							}
						}

						// If full, replace the old lookup table
						if (partialTables.size() == cbParts) {
							vqaTable = replaceLookupTable(partialTables, tablesCompressed)
						}
					}
				}

				if (!stopped) {
					logger.debug('VqaFile decoding complete')
				}
			}
		}
	}
}
