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

package nz.net.ultraq.redhorizon.codecs

import groovy.transform.CompileStatic
import java.nio.ByteBuffer

/**
 * Encoder/decoder utilizing the "XODDelta" compression scheme.
 * <p>
 * A decoder instance will retain some state after each call to {@link #decode},
 * which is useful for decoding the next image in the same sequence.  A new
 * decoder should be created for each image sequence being worked with.
 * <p>
 * For details about XORDelta, see: http://www.shikadi.net/moddingwiki/Westwood_XOR_Delta
 * Although credit goes to Vladan Bato for the description of what was
 * previously known as the "Format40" from which the code below is adapted.
 * See: http://vladan.bato.net/cnc/ccfiles4.txt
 * <p>
 * Using a notation found in XCCU, the XORDelta commands are as follows:
 * <ol>
 *   <li>00000000 c v = Xor next c bytes with v.</li>
 *   <li>0ccccccc = Xor the next c bytes from source with those in base.</li>
 *   <li>10000000 0c c = Skip the next c bytes.</li>
 *   <li>10000000 10c c = Xor the next c bytes from source with those in base.</li>
 *   <li>10000000 11c c v = Xor the next c bytes with v.</li>
 *   <li>1ccccccc = Skip the next c bytes.</li>
 * </ol>
 * 
 * @author Emanuel Rabina
 */
@CompileStatic
class XORDelta implements Encoder, Decoder {

	// Small skip command
	private static final byte CMD_SKIP_S           = (byte)0x80 // 10000000
	private static final int  CMD_SKIP_S_MAX       = 63         // 01111111, 0x7f
	private static final int  CMD_SKIP_S_THRESHOLD = 0

	// Large skip command
	private static final byte  CMD_SKIP_L1          = (byte)0x80    // 10000000
	private static final short CMD_SKIP_L2          = (short)0x0000 // 00000000 00000000
	private static final int   CMD_SKIP_L_MAX       = 32767         // 01111111 11111111, 0x7fff
//	private static final int   CMD_SKIP_L_THRESHOLD = 2

	// Small fill command
	private static final byte CMD_FILL_S           = (byte)0x00 // 00000000
	private static final int  CMD_FILL_S_MAX       = 255        // 11111111, 0xff
	private static final int  CMD_FILL_S_THRESHOLD = 2

	// Large fill command
	private static final byte  CMD_FILL_L1          = (byte)0x80    // 10000000
	private static final short CMD_FILL_L2          = (short)0xc000 // 11000000 00000000
	private static final int   CMD_FILL_L_MAX       = 16383         // 00111111 11111111, 0x3fff
//	private static final int   CMD_FILL_L_THRESHOLD = 3

	// Small XOR command
	private static final byte CMD_XOR_S           = (byte)0x00 // 00000000
	private static final byte CMD_XOR_S_MAX       = 63         // 01111111, 0x7f
//	private static final int  CMD_XOR_S_THRESHOLD = 0

	// Large XOR command
	private static final byte  CMD_XOR_L1          = (byte)0x80    // 10000000
	private static final short CMD_XOR_L2          = (short)0x8000 // 10000000 00000000
	private static final int   CMD_XOR_L_MAX       = 16383         // 00111111 11111111, 0x3fff
//	private static final int   CMD_XOR_L_THRESHOLD = 2

	private ByteBuffer xorSource

	/**
	 * Create a new decoder using an XOR source of the given size.
	 * 
	 * @param frameSize
	 */
	XORDelta(int frameSize) {

		xorSource = ByteBuffer.allocateNative(frameSize)
	}

	/**
	 * Change the base frame being used for the next {@link #decode} operation.
	 * 
	 * @param deltaSource
	 * @return This codec for chaining.
	 */
	XORDelta deltaSource(ByteBuffer deltaSource) {

		xorSource = deltaSource
		return this
	}

	@Override
	void decode(ByteBuffer source, ByteBuffer dest) {

		while (true) {
			byte command = source.get()
			int count

			// b7 = 0
			if (!(command & 0x80)) {

				// Command #1 - small XOR base with value
				if (!command) {
					count = source.get() & 0xff
					byte fill = source.get()
					while (count--) {
						dest.put((byte)(xorSource.get() ^ fill))
					}
				}
				// Command #2 - small XOR source with base for count
				else {
					count = command
					while (count--) {
						dest.put((byte)(source.get() ^ xorSource.get()))
					}
				}
			}
			// b7 = 1
			else {
				count = command & 0x7f

				// b6-0 = 0
				if (!count) {
					count = source.getShort() & 0xffff
					command = (byte)(count >>> 8)

					// b7 of next byte = 0
					if (!(command & 0x80)) {

						// Finished decoding
						if (!count) {
							break
						}

						// Command #3 - large copy base to dest for count
						dest.put(xorSource, count)
					}
					// b7 of next byte = 1
					else {
						count &= 0x3fff

						// Command #4 - large XOR source with base for count
						if (!(command & 0x40)) {
							while (count--) {
								dest.put((byte)(source.get() ^ xorSource.get()))
							}
						}
						// Command #5 - large XOR base with value
						else {
							byte fill = source.get()
							while (count--) {
								dest.put((byte)(xorSource.get() ^ fill))
							}
						}
					}
				}
				// b6-0 != 0
				else {

					// Command #6 - small copy base to dest for count
					dest.put(xorSource, count)
				}
			}
		}
		source.rewind()
		dest.flip()
		xorSource.rewind()
		xorSource = dest
	}

	@Override
	void encode(ByteBuffer source, ByteBuffer dest) {

		// Encode the source
		while (source.hasRemaining()) {

			// Select the method that provdes the best results for the coming bytes
			int skiplength = isCandidateForSkipCommand(source, xorSource)
			int filllength = isCandidateForFillCommand(source, xorSource)
			int xorlength  = isCandidateForXORCommand(source, xorSource)

			int bestmethod = Math.max(skiplength, Math.max(filllength, xorlength))

			// Either small or large skip
			if (bestmethod == skiplength) {

				// Command #6 - small skip
				if (skiplength <= CMD_SKIP_S_MAX) {
					dest.put((byte)(CMD_SKIP_S | skiplength))
				}

				// Command #3 - large skip
				else {
					dest.put(CMD_SKIP_L1)
					dest.putShort((short)(CMD_SKIP_L2 | skiplength))
				}

				source.position(source.position() + skiplength)
				xorSource.position(xorSource.position() + skiplength)
			}

			// Either small or large XOR fill
			else if (bestmethod == filllength) {
				byte xorfillval = (byte)(source.get() ^ xorSource.get())

				// Command #1 - small XOR fill
				if (filllength <= CMD_FILL_S_MAX) {
					dest.put(CMD_FILL_S)
					dest.put((byte)filllength)
				}

				// Command #5 - large XOR fill
				else {
					dest.put(CMD_FILL_L1)
					dest.putShort((short)(CMD_FILL_L2 | filllength))
				}

				dest.put(xorfillval)
				source.position(source.position() - 1 + filllength)
				xorSource.position(xorSource.position() - 1 + filllength)
			}

			// Either small or large XOR
			else {

				// Command #2 - small XOR
				if (xorlength <= CMD_XOR_S_MAX) {
					dest.put((byte)(CMD_XOR_S | xorlength))
				}

				// Command #4 - large XOR
				else {
					dest.put(CMD_XOR_L1)
					dest.putShort((short)(CMD_XOR_L2 | xorlength))
				}

				while (xorlength-- > 0) {
					dest.put((byte)(source.get() ^ xorSource.get()))
				}
			}
		}

		// SHP data must be closed by the large skip command with a length of 0
		dest.put(CMD_SKIP_L1)
		dest.putShort(CMD_SKIP_L2)

		source.rewind()
		xorSource.rewind()
		dest.flip()
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for the Format40 skip commands:
	 * <ul>
	 *   <li>3) 10000000 0c c = Skip the next c bytes.</li>
	 *   <li>6) 1ccccccc = Skip the next c bytes.</li>
	 * </ul>
	 * Buffer positions are restored after this method is through, and no
	 * alterations are made to the source data.
	 * 
	 * @param source Original raw data.
	 * @param base	 Data the Format40 is based off.
	 * @return An integer value indicating that this number of bytes can be
	 * 		   encoded using the skip command, or 0 if the following bytes
	 * 		   aren't good for the skip command.
	 */
	private static int isCandidateForSkipCommand(ByteBuffer source, ByteBuffer base) {

		// Retain current position
		source.mark()
		base.mark()

		// Find out how many bytes there are in common to skip
		int candidatelength = 0
		while (source.hasRemaining() && candidatelength < CMD_SKIP_L_MAX) {
			if (source.get() != base.get()) {
				break
			}
			candidatelength++
		}

		// Reset prior position
		source.reset()
		base.reset()

		// Evaluate skip command candidacy
		return candidatelength > CMD_SKIP_S_THRESHOLD ? candidatelength : 0
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for the Format40 XOR fill commands:
	 * <ul>
	 *   <li>1) 00000000 c v = Xor next c bytes with v.</li>
	 *   <li>5) 10000000 11c c v = Xor the next c bytes with v.</li>
	 * </ul>
	 * Buffer positions are restored after this method is through, and no
	 * alterations are made to the source data.
	 * 
	 * @param source Original raw data.
	 * @param base	 Data the Format40 is based off.
	 * @return An integer value indicating that this number of bytes can be
	 * 		   encoded using the fill command, or 0 if the following bytes
	 * 		   aren't good for the fill command.
	 */
	private static int isCandidateForFillCommand(ByteBuffer source, ByteBuffer base) {

		// Retain current position
		source.mark()
		base.mark()

		// Find out how many similar bytes can be XOR'ed over contiguous base data
		int candidatelength = 1
		byte sourcebyte = source.get()
		byte basebyte   = base.get()

		while (source.hasRemaining() && candidatelength < CMD_FILL_L_MAX) {
			if (source.get() != sourcebyte || base.get() != basebyte) {
				break
			}
			candidatelength++
		}

		// Reset prior position
		source.reset()
		base.reset()

		// Evaluate skip command candidacy
		return candidatelength > CMD_FILL_S_THRESHOLD ? candidatelength : 0
	}

	/**
	 * Performs a test on the data to determine whether it is a good candidate
	 * for the Format40 XOR commands:
	 * <ul>
	 *   <li>2) 0ccccccc = Xor the next c bytes from source with those in base.<li>
	 *   <li>4) 10000000 10c c = Xor the next c bytes from source with those in base.</li>
	 * </ul>
	 * Buffer positions are restored after this method is through, and no
	 * alterations are made to the source data.
	 * 
	 * @param source Original raw data.
	 * @param base	 Data the Format40 is based off.
	 * @return An integer value indicating that this number of bytes can be
	 * 		   encoded using the XOR command.  Unlike other methods, this
	 * 		   check always returns a positive number.
	 */
	private static int isCandidateForXORCommand(ByteBuffer source, ByteBuffer base) {

		// Retain current position
		source.mark()
		base.mark()

		// Find out how many dissimilar bytes can be encoded with the XOR command
		int candidatelength = 1
		int runlength = 1
		byte lastsourcebyte = source.get()
		byte lastbasebyte   = base.get()

		while (source.hasRemaining() && candidatelength < CMD_XOR_L_MAX) {
			byte nextsourcebyte = source.get()
			byte nextbasebyte   = base.get()

			if (nextsourcebyte == lastsourcebyte && nextbasebyte == lastbasebyte) {
				runlength++
				if (runlength == 2) {
					candidatelength -= runlength - 2
					break
				}
			}
			else {
				runlength = 1
			}
			candidatelength++
			lastsourcebyte = nextsourcebyte
			lastbasebyte   = nextbasebyte
		}

		// Reset prior position
		source.reset()
		base.reset()

		// Evaluate skip command candidacy
		return candidatelength
	}
}
