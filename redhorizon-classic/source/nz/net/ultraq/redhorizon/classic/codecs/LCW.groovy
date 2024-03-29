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
			int[] offsetpart = isCandidateForOffsetCopyCommand(source)
			int[] copypart = isCandidateForCopyCommand(source)
			int filllength = isCandidateForFillCommand(source)
			int xferlength = isCandidateForTransferCommand(source)

			int bestmethod = Math.max(offsetpart[0], Math.max(copypart[0], Math.max(filllength, xferlength)))

			// Command #4 - run-length encoding, aka: fill
			if (bestmethod == filllength) {
				byte colourval = source.get()

				dest.put(CMD_FILL)
				dest.putShort((short)filllength)
				dest.put(colourval)

				source.position(source.position() - 1 + filllength)
			}

			// Command #1 - offset copy
			else if (bestmethod == offsetpart[0]) {
				dest.put((byte)(CMD_OFFSET | ((offsetpart[0] - 3) << 4) | (offsetpart[1] >>> 8)))
				dest.put((byte)offsetpart[1])

				source.position(source.position() + offsetpart[0])
			}

			// Either small or large copy
			else if (bestmethod == copypart[0]) {

				// Command #3 - small copy
				if (copypart[0] <= CMD_COPY_S_MAX) {
					dest.put((byte)(CMD_COPY_S | (copypart[0] - 3)))
					dest.putShort((short)copypart[1])
				}

				// Command #5 - large copy
				else {
					dest.put(CMD_COPY_L)
					dest.putShort((short)copypart[0])
					dest.putShort((short)copypart[1])
				}

				source.position(source.position() + copypart[0])
			}

			// Command #2 - straight transfer of bytes from source to dest
			else {
				byte[] xferbytes = new byte[xferlength]
				source.get(xferbytes)

				dest.put((byte)(CMD_TRANSFER | xferlength))
				dest.put(xferbytes)
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
	 * Buffer positions are restored after this method is through, and no
	 * alterations are made to the source data.
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

		return source.markAndReset {

			// Copy of the bytes read thus far and within the offset range limit
			ByteBuffer sourcecopy = source.duplicate()
			sourcecopy.limit(source.position())
			sourcecopy.position(Math.max(0, source.position() - CMD_OFFSET_RANGE))

			int candidatelength   = 0
			int candidateposition = -1

			// Search for instances of the remaining bytes in the source range
			int copypos = sourcecopy.position()
			while (source.hasRemaining() && sourcecopy.hasRemaining()) {
				sourcecopy.mark()

				// Potential match
				int runlength = 0
				while (source.hasRemaining() && sourcecopy.hasRemaining() && runlength < CMD_OFFSET_MAX) {
					if (source.get() == sourcecopy.get()) {
						runlength++
					}
					else {
						break
					}
				}
				source.reset()
				sourcecopy.reset()

				// Update candidate length and position?
				if (runlength > candidatelength) {
					candidatelength = runlength
					candidateposition = source.position() - copypos
				}

				sourcecopy.position(++copypos)
			}

			// Evaluate offset copy command candidacy
			return [
				candidatelength > CMD_OFFSET_THRESHOLD ? candidatelength : 0,
				candidateposition
			] as int[]
		}
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for either Format80 copy command:
	 * <ul>
	 *   <li>3) 11cccccc p p = Copy c + 3 bytes from p</li>
	 *   <li>5) 11111111 c c p p = Copy c bytes from p</li>
	 * </ul>
	 * Buffer positions are restored after this method is through, and no
	 * alterations are made to the source data.
	 * 
	 * @param source Original raw data.
	 * @return The first element indicates that this number of bytes can be
	 * 		   encoded using the copy command, or 0 if the following bytes
	 * 		   aren't good for the copy command.  The second integer is the
	 * 		   position in the source buffer that these bytes occur, if
	 * 		   the first is not 0.
	 */
	private static int[] isCandidateForCopyCommand(ByteBuffer source) {

		return source.markAndReset {

			// Copy of the bytes read thus far
			ByteBuffer sourcecopy = source.duplicate()
			sourcecopy.limit(Math.min(source.position(), CMD_COPY_L_MAX * 2))
			sourcecopy.position(0)

			int candidatelength   = 0
			int candidateposition = -1

			// Search for instances of the remaining bytes in the source so far
			int copypos = 0
			while (source.hasRemaining() && sourcecopy.hasRemaining() && copypos < CMD_COPY_L_MAX) {
				sourcecopy.mark()

				// Potential match
				int runlength = 0
				while (source.hasRemaining() && sourcecopy.hasRemaining() && runlength < CMD_COPY_L_MAX) {
					if (source.get() == sourcecopy.get()) {
						runlength++
					}
					else {
						break
					}
				}
				source.reset()
				sourcecopy.reset()

				// Update candidate length and position?
				if (runlength > candidatelength) {
					candidatelength = runlength
					candidateposition = copypos
				}

				sourcecopy.position(++copypos)
			}

			// Evaluate copy command candidacy
			return [
				candidatelength > CMD_COPY_S_THRESHOLD ? candidatelength : 0,
				candidateposition
			] as int[]
		}
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for the Format80 fill command (similar to run-length encoding):
	 * <ul>
	 *   <li>4) 11111110 c c v = Write c bytes with v</li>
	 * </ul>
	 * Buffer positions are restored after this method is through, and no
	 * alterations are made to the source data.
	 * 
	 * @param source Original raw data.
	 * @return An integer value indicating that this number of bytes can be
	 * 		   encoded using the fill command, with 0 indicating that the
	 * 		   following bytes aren't good for the fill command.
	 */
	private static int isCandidateForFillCommand(ByteBuffer source) {

		return source.markAndReset {

			// Find out how many bytes ahead have the same value as the starting byte
			int candidatelength = 1
			byte fillbyte = source.get()

			while (source.hasRemaining() && candidatelength < CMD_FILL_MAX) {
				if (fillbyte != source.get()) {
					break
				}
				candidatelength++
			}

			// Evaluate fill command candidacy
			return candidatelength > CMD_FILL_THRESHOLD ? candidatelength : 0
		}
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for the Format80 transfer command:
	 * <ul>
	 *   <li>2) 10cccccc = Copy next c bytes from source to dest</li>
	 * </ul>
	 * Buffer positions are restored after this method is through, and no
	 * alterations are made to the source data.
	 * 
	 * @param source Original raw data.
	 * @return An integer value indicating that this number of bytes can be
	 * 		   encoded using the transfer command.  Unlike other methods,
	 * 		   this test always returns a positive result.
	 */
	private static int isCandidateForTransferCommand(ByteBuffer source) {

		return source.markAndReset {

			// Find out the longest stretch of dissimilar bytes
			int candidatelength = 1
			int runlength = 1
			byte lastbyte = source.get()

			while (source.hasRemaining() && candidatelength < CMD_TRANSFER_MAX) {
				byte nextbyte = source.get()
				if (nextbyte == lastbyte) {
					runlength++
					if (runlength > CMD_FILL_THRESHOLD) {
						candidatelength -= runlength - 2
						break
					}
				}
				else {
					runlength = 1
				}
				candidatelength++
				lastbyte = nextbyte
			}

			// Transfer command candidacy is always valid
			return candidatelength
		}
	}
}
