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

package nz.net.ultraq.redhorizon.classic.codecs

import nz.net.ultraq.redhorizon.filetypes.codecs.Decoder
import nz.net.ultraq.redhorizon.filetypes.codecs.Encoder

import groovy.transform.CompileStatic
import java.nio.ByteBuffer

/**
 * Encoder/decoder utilizing the "LCW" compression scheme.
 * <p>
 * For details about LCW, see:
 * <a href="http://www.shikadi.net/moddingwiki/Westwood_LCW" target="_top">http://www.shikadi.net/moddingwiki/Westwood_LCW</a>
 * Although credit goes to Vladan Bato for the description of what was
 * previously known as the "Format80" from which the code below is adapted.
 * See: <a href="http://vladan.bato.net/cnc/ccfiles4.txt" target="_top">http://vladan.bato.net/cnc/ccfiles4.txt</a>
 * <p>
 * Using a notation found in XCCU, the LCW commands are as follows:
 * <ol>
 *   <li>0cccpppp p = Copy c + 3 bytes from dest.pos - p to dest.pos</li>
 *   <li>10cccccc = Copy next c bytes from source to dest</li>
 *   <li>11cccccc p p = Copy c + 3 bytes from p</li>
 *   <li>11111110 c c v = Write c bytes with v</li>
 *   <li>11111111 c c p p = Copy c bytes from p</li>
 * </ol>
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class LCW implements Encoder, Decoder {

	// @formatter:off
	// Transfer command
	private static final byte CMD_TRANSFER       = (byte)0x80 // 10000000
	private static final int  CMD_TRANSFER_MAX	 = 63         // 00111111, 0x3f

	// Offset copy command
	private static final byte CMD_OFFSET           = (byte)0x00 // 00000000
	private static final int  CMD_OFFSET_MAX       = 10         // -3 = 111, 0x07
	private static final int  CMD_OFFSET_THRESHOLD = 2          // Must encode at least 3 bytes
	private static final int  CMD_OFFSET_RANGE     = 4095       // 00001111 11111111, 0x0fff

	// Small copy command
	private static final byte CMD_COPY_S           = (byte)0xc0 // 11000000
	private static final int  CMD_COPY_S_MAX       = 64         // -3 = 00111101, 0x3d
	private static final int  CMD_COPY_S_THRESHOLD = 2

	// Large copy command
	private static final byte CMD_COPY_L           = (byte)0xff // 11111111
	private static final int  CMD_COPY_L_MAX       = 65535      // 11111111 11111111, 0xffff
//	private static final int  CMD_COPY_L_THRESHOLD = 4

	// Colour command
	private static final byte CMD_FILL             = (byte)0xfe // 11111110
	private static final int  CMD_FILL_MAX         = 65535      // 11111111 11111111, 0xffff
	private static final int  CMD_FILL_THRESHOLD   = 3
	// @formatter:on

	@Override
	ByteBuffer decode(ByteBuffer source, ByteBuffer dest) {

		while (true) {
			byte command = source.get()
			int count, copyPos

			// b7 = 0
			if (!(command & 0x80)) {

				// Command #1 - copy bytes relative to the current position in dest.
				// This can overlap with the current position, so a bulk copy is not
				// easily doable.
				count = (command >>> 4) + 3
				copyPos = dest.position() - (((command & 0x0f) << 8) | (source.get() & 0xff))
				while (count--) {
					dest.put(dest.get(copyPos++))
				}
			}
			// b7 = 1
			else {
				count = command & 0x3f

				// b6 = 0
				if (!(command & 0x40)) {

					// Finished decoding
					if (!count) {
						break
					}

					// Command #2 - copy the next count bytes as is from source to dest.
					dest.put(source, count)
				}
				// b6 = 1
				else {

					// Command #3 - copy bytes from the given position in dest.
					if (count < 0x3e) {
						count += 3
						copyPos = source.getShort() & 0xffff
						while (count--) {
							dest.put(dest.get(copyPos++))
						}
					}
					// Command #4 - fill dest with the next byte for up to count bytes.
					else if (count == 0x3e) {
						count = source.getShort() & 0xffff
						byte fill = source.get()
						while (count--) {
							dest.put(fill)
						}
					}
					// Command #5 - copy bytes from the given position in dest.
					else {
						count = source.getShort() & 0xffff
						copyPos = source.getShort() & 0xffff
						while (count--) {
							dest.put(dest.get(copyPos++))
						}
					}
				}
			}
		}
		source.rewind()
		return dest.flip()
	}

	@Override
	ByteBuffer encode(ByteBuffer source, ByteBuffer dest) {

		// Format80 data must be opened by the transfer command
		dest.put((byte)(CMD_TRANSFER | 1))
		dest.put(source.get())

		// Encode the source
		while (source.hasRemaining()) {

			// Select the method that provdes the best results for the coming bytes
			var offsetPart = isCandidateForOffsetCopyCommand(source)
			var copyPart = isCandidateForCopyCommand(source)
			var fillLength = isCandidateForFillCommand(source)
			var xferLength = isCandidateForTransferCommand(source)

			var bestMethod = Math.max(offsetPart[0], Math.max(copyPart[0], Math.max(fillLength, xferLength)))

			// Command #4 - run-length encoding, aka: fill
			if (bestMethod == fillLength) {
				var colourVal = source.get()

				dest.put(CMD_FILL)
				dest.putShort((short)fillLength)
				dest.put(colourVal)

				source.position(source.position() - 1 + fillLength)
			}

			// Command #1 - offset copy
			else if (bestMethod == offsetPart[0]) {
				dest.put((byte)(CMD_OFFSET | ((offsetPart[0] - 3) << 4) | (offsetPart[1] >>> 8)))
				dest.put((byte)offsetPart[1])

				source.position(source.position() + offsetPart[0])
			}

			// Either small or large copy
			else if (bestMethod == copyPart[0]) {

				// Command #3 - small copy
				if (copyPart[0] <= CMD_COPY_S_MAX) {
					dest.put((byte)(CMD_COPY_S | (copyPart[0] - 3)))
					dest.putShort((short)copyPart[1])
				}

				// Command #5 - large copy
				else {
					dest.put(CMD_COPY_L)
					dest.putShort((short)copyPart[0])
					dest.putShort((short)copyPart[1])
				}

				source.position(source.position() + copyPart[0])
			}

			// Command #2 - straight transfer of bytes from source to dest
			else {
				var xferBytes = new byte[xferLength]
				source.get(xferBytes)

				dest.put((byte)(CMD_TRANSFER | xferLength))
				dest.put(xferBytes)
			}
		}

		// SHP data must be closed by the transfer command w/ a length of 0
		dest.put(CMD_TRANSFER)

		source.rewind()
		return dest.flip()
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for the Format80 offset copy command:
	 * <ul>
	 *   <li>1) 0cccpppp p = Copy c + 3 bytes from dest.pos - p to dest.pos</li>
	 * </ul>
	 *
	 * @param source Original raw data.
	 * @return The first element indicates that this number of bytes can be
	 * 		   encoded using the offset copy command, or 0 if the following
	 * 		   bytes aren't good for the offset copy command.  The second
	 * 		   integer is the position in the source buffer, relative to the
	 * 		   current source position, that these bytes occur, if the first
	 * 		   is not 0.
	 */
	private static int[] isCandidateForOffsetCopyCommand(ByteBuffer source) {

		// For traversing the source data for matching patterns
		var sourceCompareCopy = source.duplicate()
		sourceCompareCopy.mark()

		// Copy of the bytes read thus far and within the offset range limit
		var sourceCopy = source.duplicate()
		sourceCopy.limit(source.position())
		sourceCopy.position(Math.max(0, source.position() - CMD_OFFSET_RANGE))

		var candidateLength = 0
		var candidatePosition = -1

		// Search for instances of the remaining bytes in the source range
		var copyPos = sourceCopy.position()
		while (sourceCompareCopy.hasRemaining() && sourceCopy.hasRemaining()) {
			sourceCopy.mark()

			// Potential match
			var runLength = 0
			while (sourceCompareCopy.hasRemaining() && sourceCopy.hasRemaining() && runLength < CMD_OFFSET_MAX) {
				if (sourceCompareCopy.get() == sourceCopy.get()) {
					runLength++
				}
				else {
					break
				}
			}
			sourceCompareCopy.reset()
			sourceCopy.reset()

			// Update candidate length and position?
			if (runLength > candidateLength) {
				candidateLength = runLength
				candidatePosition = sourceCompareCopy.position() - copyPos
			}

			sourceCopy.position(++copyPos)
		}

		// Evaluate offset copy command candidacy
		return [candidateLength > CMD_OFFSET_THRESHOLD ? candidateLength : 0, candidatePosition] as int[]
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for either Format80 copy command:
	 * <ul>
	 *   <li>3) 11cccccc p p = Copy c + 3 bytes from p</li>
	 *   <li>5) 11111111 c c p p = Copy c bytes from p</li>
	 * </ul>
	 *
	 * @param source Original raw data.
	 * @return
	 *   The first element indicates that this number of bytes can be encoded
	 *   using the copy command, or 0 if the following bytes aren't good for the
	 *   copy command.  The second integer is the position in the source buffer
	 *   that these bytes occur, if the first is not 0.
	 */
	private static int[] isCandidateForCopyCommand(ByteBuffer source) {

		// For traversing the source data for matching patterns
		var sourceCompareCopy = source.duplicate()
		sourceCompareCopy.mark()

		// Copy of the bytes read thus far
		var sourceCopy = source.duplicate()
		sourceCopy.limit(Math.min(source.position(), CMD_COPY_L_MAX * 2))
		sourceCopy.position(0)

		var candidatelength = 0
		var candidateposition = -1

		// Search for instances of the remaining bytes in the source so far
		var copyPos = 0
		while (sourceCompareCopy.hasRemaining() && sourceCopy.hasRemaining() && copyPos < CMD_COPY_L_MAX) {
			sourceCopy.mark()

			// Potential match
			var runLength = 0
			while (sourceCompareCopy.hasRemaining() && sourceCopy.hasRemaining() && runLength < CMD_COPY_L_MAX) {
				if (sourceCompareCopy.get() == sourceCopy.get()) {
					runLength++
				}
				else {
					break
				}
			}
			sourceCompareCopy.reset()
			sourceCopy.reset()

			// Update candidate length and position?
			if (runLength > candidatelength) {
				candidatelength = runLength
				candidateposition = copyPos
			}

			sourceCopy.position(++copyPos)
		}

		// Evaluate copy command candidacy
		return [candidatelength > CMD_COPY_S_THRESHOLD ? candidatelength : 0, candidateposition] as int[]
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for the Format80 fill command (similar to run-length encoding):
	 * <ul>
	 *   <li>4) 11111110 c c v = Write c bytes with v</li>
	 * </ul>
	 *
	 * @param source Original raw data.
	 * @return
	 *   An integer value indicating that this number of bytes can be encoded
	 *   using the fill command, with 0 indicating that the following bytes aren't
	 *   good for the fill command.
	 */
	private static int isCandidateForFillCommand(ByteBuffer source) {

		var sourceCopy = source.duplicate()

		// Find out how many bytes ahead have the same value as the starting byte
		var candidateLength = 1
		var fillByte = sourceCopy.get()

		while (sourceCopy.hasRemaining() && candidateLength < CMD_FILL_MAX) {
			if (fillByte != sourceCopy.get()) {
				break
			}
			candidateLength++
		}

		// Evaluate fill command candidacy
		return candidateLength > CMD_FILL_THRESHOLD ? candidateLength : 0
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for the Format80 transfer command:
	 * <ul>
	 *   <li>2) 10cccccc = Copy next c bytes from source to dest</li>
	 * </ul>
	 *
	 * @param source Original raw data.
	 * @return
	 *   An integer value indicating that this number of bytes can be encoded
	 *   using the transfer command.  Unlike other methods, this test always
	 *   returns a positive result.
	 */
	private static int isCandidateForTransferCommand(ByteBuffer source) {

		var sourceCopy = source.duplicate()

		// Find out the longest stretch of dissimilar bytes
		var candidateLength = 1
		var runLength = 1
		var lastbyte = sourceCopy.get()

		while (sourceCopy.hasRemaining() && candidateLength < CMD_TRANSFER_MAX) {
			var nextByte = sourceCopy.get()
			if (nextByte == lastbyte) {
				runLength++
				if (runLength > CMD_FILL_THRESHOLD) {
					candidateLength -= runLength - 2
					break
				}
			}
			else {
				runLength = 1
			}
			candidateLength++
			lastbyte = nextByte
		}

		// Transfer command candidacy is always valid
		return candidateLength
	}
}
