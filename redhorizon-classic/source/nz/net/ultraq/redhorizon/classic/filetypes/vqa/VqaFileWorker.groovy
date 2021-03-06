/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.codecs.Decoder
import nz.net.ultraq.redhorizon.classic.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.classic.filetypes.VgaPalette
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import java.nio.ByteBuffer

/**
 * A worker for decoding VQA file video data.
 * 
 * TODO: Try speed up decoding here by delegating sound/frame data to new
 *       threads.  That could remove a lot of the @CompileStatic we've been
 *       leaning on.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
class VqaFileWorker extends Worker {

	private static final Logger logger = LoggerFactory.getLogger(VqaFileWorker)

	@Delegate
	final VqaFile vqaFile
	final NativeDataInputStream input

	private final LCW lcw = new LCW()
	private final Decoder audioDecoder

	// Precalculated values to aid frame decoding
	private final int blocksX
	private final int blockSize
	private final int modifier
	private final int numBlocks

	/**
	 * Constructor, create a new worker for decoding the VQA video data.
	 * 
	 * @param vqaFile
	 * @param input
	 */
	VqaFileWorker(VqaFile vqaFile, NativeDataInputStream input) {

		this.vqaFile = vqaFile
		this.input = input

		audioDecoder = bits == 16 ? new IMAADPCM16bit() : new WSADPCM8bit()

		blocksX   = (width / blockWidth)
		blockSize = blockWidth * blockHeight
		modifier  = blockHeight == 2 ? 0xf : 0xff
		numBlocks = blocksX * (height / blockHeight)
	}

	/**
	 * Decodes a frame of a video, found in a VPT* chunk.
	 * 
	 * This method was split from the other one of the same name so it could be
	 * optimized for performance, utilizing {@code @CompileStatic} and mostly
	 * standard Java coding.
	 * 
	 * @param data     The VPT chunk data.
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
	 * @param data       The VPT chunk data.
	 * @param codeBook   Current lookup table for screen block data.
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
	 * @param data   The SND chunk data.
	 * @return The sound data from this chunk.
	 */
	private ByteBuffer decodeSound(VqaChunkHeader header, ByteBuffer data) {

		return header.dataCompressed ?
			audioDecoder.decode(data, ByteBuffer.allocateNative(header.length << 2)) : // IMA ADPCM is always 4x the compression
			data
	}

	@Override
	void work() {

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
		while (canContinue && input.bytesRead < formLength) {
			def chunkHeader = new VqaChunkHeader(input)

			switch (chunkHeader.name) {

			// Decode sound data
			case ~/SND./:
				def sample = decodeSound(chunkHeader, ByteBuffer.wrapNative(input.readNBytes(chunkHeader.length)))
				trigger(new StreamingSampleEvent(sample))
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
		}

		if (!stopped) {
			logger.debug('Decoding complete')
		}
	}
}
