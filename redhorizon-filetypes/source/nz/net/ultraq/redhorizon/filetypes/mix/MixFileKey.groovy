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

package nz.net.ultraq.redhorizon.filetypes.mix

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * Class for the native logic of obtaining the 56 byte Blowfish key from an 80
 * byte key source found in encrypted MIX files.
 * <p>
 * This code was originally adapted from that found in the documentation of the
 * MIX format by XCC Utilities author Olaf van der Spek.  It has since gone
 * through several revisions from the native C/C++ code to lean on as much Java
 * and Groovy built-ins as possible.  Some of the native code still remains
 * however, as I'm not entirely sure what the intent of the original code was.
 * <p>
 * From what I've gathered, it converts the public key string and source data
 * found in the MIX files into "big numbers" (arbitrary precision integers) and
 * performs a bunch of big number math on those values to get our 56 byte key.
 * 
 * @author Emanuel Rabina
 */
class MixFileKey {

	private static final Logger logger = LoggerFactory.getLogger(MixFileKey)

	static final int SIZE_KEY_SOURCE = 80
	static final int SIZE_KEY = 56

	// TODO: The key values are constant, so maybe just save those instead
	//       of running the calculations every time?
	// A base64 encoded string of the public key used for the transformation function
	static final String PUBLIC_KEY_STRING = 'AihRvNoIbTn85FZRYNZRcT+i6KpU+maCsEqr3Q5q+LDB5tH7Tz2qQ38V'

	private static final BigInteger bitMask
	private static final BigInteger publicKey

	static {
		bitMask = new BigInteger([0x00, 0x01, 0x00, 0x01] as byte[])

		def publicKeyBytes = ByteBuffer.wrap(Base64.getDecoder().decode(PUBLIC_KEY_STRING))
		assert publicKeyBytes.get() == 2

		// Get the length (in bytes) of the base64 decoded key string.  This is
		// usually obtained in the second byte of the decoded string, but if the
		// sign bit of that byte is set, then that byte is the number of following
		// bytes that need to be read to construct the length value.
		def nextByte = publicKeyBytes.get()
		def keyLength = 0
		if (nextByte & 0x80) {
			for (int i = 0; i < (nextByte & 0x7f); i++) {
				keyLength <<= 8
				keyLength |= publicKeyBytes.get() & 0xff
			}
		}
		else {
			keyLength = nextByte
		}

		publicKey = new BigInteger(publicKeyBytes.array(), publicKeyBytes.position(), keyLength)
	}

	private BigInteger global1
	private int g1lengthx2

	private BigInteger g1hi
	private BigInteger g1hiinv
	private int g1hibitlength
	private int g1hiinvlo
	private int g1hiinvhi

	private BigInteger global2

	/**
	 * Calculate the number of bytes needed to be able to represent the given
	 * number of bits.
	 * 
	 * @param bits
	 * @return
	 */
	private static int fitBytes(int bits) {

		return (bits + 7) >>> 3
	}

	/**
	 * Calculate the number of ints needed to be able to represent the given
	 * number of bits.
	 * 
	 * @param bits
	 * @return
	 */
	private static int fitInts(int bits) {

		return (bits + 31) >>> 5
	}

	/**
	 * Calculate the number of shorts needed to be able to represent the given
	 * number of bits.
	 * 
	 * @param bits
	 * @return
	 */
	private static int fitShorts(int bits) {

		return (bits + 15) >>> 4
	}

	/**
	 * Convert a little endian byte array to a {@code BigInteger}.
	 * 
	 * @param bytes
	 * @return
	 */
	private static BigInteger fromLittleEndianByteArray(byte[] bytes) {

		return fromLittleEndianByteArray(bytes, 0, bytes.length)
	}

	/**
	 * Convert a little endian byte array to a {@code BigInteger}.
	 *
	 * @param bytes
	 * @param offset
	 * @param length
	 * @return
	 */
	private static BigInteger fromLittleEndianByteArray(byte[] bytes, int offset, int length) {

		// TODO: This should be a "reverse" extension
		byte[] flipped = new byte[bytes.length]
		for (int i = 0; i < bytes.length; i++) {
			flipped[i] = bytes[bytes.length - 1 - i]
		}
		return new BigInteger(flipped, flipped.length - length - offset, length)
	}

	/**
	 * Convert a {@code BigInteger}'s big endian byte representation to a little
	 * endian byte array.
	 * 
	 * @param value
	 * @return
	 */
	private static byte[] toLittleEndianByteArray(BigInteger value) {

		byte[] bytes = value.toByteArray()

		// Strip out any bytes representing a sign bit
		if (bytes[0] == 0) {
			byte[] newBytes = new byte[bytes.length - 1]
			System.arraycopy(bytes, 1, newBytes, 0, newBytes.length)
			bytes = newBytes
		}

		// TODO: This should be a "reverse" extension
		byte[] flipped = new byte[bytes.length]
		for (int i = 0; i < bytes.length; i++) {
			flipped[i] = bytes[bytes.length - 1 - i]
		}
		return flipped
	}

	/**
	 * Calculates the 56-byte Blowfish key from the 80-byte key source found in
	 * Red Alert's MIX files.
	 * 
	 * @param source A buffer containing the 80-byte key source.
	 * @return A buffer containing the 56-byte Blowfish key.
	 */
	ByteBuffer calculateKey(ByteBuffer source) {

		def key = ByteBuffer.allocateNative(SIZE_KEY)

		// Process the key in parts that can fit the public key
		// TODO: Can maybe write some split function for a ByteBuffer so this can be
		//       iterated over?
		def processingSize = fitBytes(publicKey.bitLength())
		while (source.hasRemaining()) {
			def partialSource = new byte[processingSize]
			source.get(partialSource)
			def bigIntegerPartialSource = fromLittleEndianByteArray(partialSource)

			def bigIntegerPartialKey = calculateKeyBigNumber(bigIntegerPartialSource)

			def keyArray = toLittleEndianByteArray(bigIntegerPartialKey)
			key.put(keyArray, 0, keyArray.length)
		}
		source.rewind()
		return key.rewind()
	}

	/**
	 * Calculate the key now that all sources have been transformed into
	 * {@code BigInteger}s.
	 * 
	 * @param source
	 * @return The key, as a {@code BigInteger}.
	 */
	private BigInteger calculateKeyBigNumber(BigInteger source) {

		initTwoInts()

		def bitMaskBitLength = bitMask.bitLength()

		// TODO: This is a mask using the bitMask value, but shifted right to begin
		//       with, then cycled as a 32-bit/int value for only 16 bits in the
		//       following loop.  Do we really need the global bitMask then?
		def bitmask = (1 << ((bitMaskBitLength - 1) % 32)) >> 1

		// TODO: This bytebuffer cycles through bytes from the end to the beginning.
		//       That seems like going from the most-significant byte to the least,
		//       so should I just order it in big endian order to begin with and
		//       iterate "upwards" instead?
		def n3Bytes = ByteBuffer.wrapNative(toLittleEndianByteArray(bitMask))
		n3Bytes.position(n3Bytes.limit() - 1)
		bitMaskBitLength--

		// TODO: A copy of the source that gets mutated by the key calculation
		def key = new BigInteger(source.toString())

		while (--bitMaskBitLength >= 0) {

			// TODO: A wrapping of the bit mask.  This wrapping bit pattern is done a
			//       few times in this file, so maybe warrants an extension or at
			//       least some function to call.
			if (bitmask == 0) {
				bitmask = 0x80000000
				n3Bytes.position(n3Bytes.position() - 1)
			}
			key = calculateKeyBigNumber(key, key)

			if (n3Bytes.get(n3Bytes.position()) & bitmask) {
				key = calculateKeyBigNumber(key, source)
			}
			bitmask >>>= 1
		}
		clearTempVars()
		return key
	}

	/**
	 * Initializes several temporary variables from the public key.
	 */
	private void initTwoInts() {

		// TODO: If I can figure out the scope for these values, then these globals
		//       should be removable.
		global1 = new BigInteger(publicKey.toString())
		g1lengthx2 = fitShorts(global1.bitLength())

		// Uses the move as a small copy
		// TODO: Why -32?  Is that just to cut it in half?
		g1hi = new BigInteger(global1.toByteArray(), 0, 8)
		g1hibitlength = g1hi.bitLength() - 32

		// Should right shift as many bits as it contains so that it ends up in the lower half
		// TODO: Looks like g1hi is only used to split it into the other values.
		g1hi = g1hi.shiftRight(g1hibitlength)
		g1hiinv = bigNumberInverse(g1hi).shiftRight(1)

		g1hibitlength = (g1hibitlength + 15) % 16 + 1
		g1hiinv = g1hiinv.add(BigInteger.ONE)

		if (g1hiinv.bitLength() > 32) {
			g1hiinv = g1hiinv.shiftRight(1)
			g1hibitlength--
		}
		// By this point, g1hiinv is reduced to a 32 bit value, but there may be 5
		// bytes in the array to represent leading 0s (sign bits)
		def g1hiinvBytes = g1hiinv.toByteArray()
		def length = g1hiinvBytes.length

		g1hiinvlo = (g1hiinvBytes[length - 2] & 0xff) << 8 | (g1hiinvBytes[length - 1] & 0xff)
		g1hiinvhi = (g1hiinvBytes[length - 4] & 0xff) << 8 | (g1hiinvBytes[length - 3] & 0xff)
	}

	/**
	 * Inverse function?
	 * <p>
	 * I'm not really sure what the original C++ function was supposed to be doing
	 * because it wasn't following any definition of "inverse" I knew or could
	 * find.  As such, I'm keeping it mostly intact and translating it to
	 * Java/Groovy so it can continue working.
	 * 
	 * @param value The original value to be inverted.
	 * @return The "inverse" of {@code value}.
	 */
	private BigInteger bigNumberInverse(BigInteger value) {

		// The original C++ code worked on byte arrays, working on them as ints and
		// doing BigNumber arithmetic, so what follows is pretty nuts.

		def tempBytes = ByteBuffer.allocate(256).order(ByteOrder.nativeOrder())
		def tempInts = tempBytes.asIntBuffer()
		def dest = new int[4]
		def destPos = 0

		def bitLength = value.bitLength()
		def bit = 1 << (bitLength % 32)
		destPos += fitInts(bitLength)
		def intLength = fitInts(bitLength)
		tempInts.put(intLength / 4 as int, tempInts.get(tempInts.position()) | (1 << ((bitLength - 1) & 0x1f)))

		def temp = fromLittleEndianByteArray(tempBytes.array())

		while (bitLength--) {
			temp = temp.shiftLeft(1)
			if (temp <=> value != -1) {
				temp = temp.subtract(value)
				dest[destPos] |= bit
			}
			bit >>>= 1
			// TODO: Another instance of bit shifting with wrapping
			if (bit == 0) {
				destPos--
				bit = 0x80000000
			}
		}

		// TODO: A byte view over an int-sized array?
		def destBytes = new byte[16]
		for (int i = 0; i < destBytes.length; i++) {
			destBytes[i] = (byte)(dest[i / 4 as int] >>> (8 * (i % 4)))
		}

		return fromLittleEndianByteArray(destBytes)
	}

	/**
	 * Further calculation of the key based on the given 2 sources values.
	 * 
	 * @param source1
	 * @param source2
	 * @return Key value calculated from the 2 sources.
	 */
	private BigInteger calculateKeyBigNumber(BigInteger source1, BigInteger source2) {

		global2 = source1 * source2

		def g2lengthx2 = fitInts(global2.bitLength()) * 2
		if (g2lengthx2 >= g1lengthx2) {
			global2 = global2.add(BigInteger.ONE).negate()

			def lengthdiff = g2lengthx2 + 1 - g1lengthx2

			def global2RawBytes = toLittleEndianByteArray(global2)
			def global2Bytes = global2 <=> BigInteger.ZERO == -1 ?
				ByteBuffer.allocateNative(global2RawBytes.length + 4)
					.put(global2RawBytes)
					.putInt(-1) // Mock sign int
					.rewind() :
				ByteBuffer.wrapNative(global2RawBytes)
			ShortBuffer global2Shorts = global2Bytes.asShortBuffer()
			def esi = lengthdiff
			def edi = g2lengthx2 + 1

			for (; lengthdiff > 0; lengthdiff--) {
				esi--
				edi--
				int temp = getMulShort(global2Shorts.position(edi))

				if (temp > 0) {
					// TODO: This deconstruct/reconstruct is the splitting of a BigInteger
					//       from a specific point to localize a calculation to just that
					//       part of the number.  Could be a good extension method?
					def global2BytesArray = global2Bytes.array()
					def partial = fromLittleEndianByteArray(global2BytesArray, esi * 2, global2BytesArray.length - (esi * 2))
					partial = (global1 * new BigInteger(Integer.toString(temp))).add(partial)

					// Reconstruct the value
					ByteBuffer allBytes = global2Bytes.duplicate()
					byte[] partialArray = toLittleEndianByteArray(partial)
					allBytes.position(esi * 2).put(partialArray)
					while (allBytes.hasRemaining()) {
						allBytes.put((byte)0xff) // Fill remaining with the sign bit
					}
					global2 = fromLittleEndianByteArray(allBytes.array())

					if ((global2Shorts.get(edi) & 0x8000) == 0) {
						// TODO: Another split calculation
						def partial2 = fromLittleEndianByteArray(global2BytesArray, esi * 2, global2BytesArray.length - (esi * 2))
						partial2 = partial2.subtract(global1)

						// Reconstruct the value
						allBytes = global2Bytes.duplicate()
						def partial2Array = toLittleEndianByteArray(partial2)
						allBytes.position(esi * 2).put(partial2Array)
						while (allBytes.hasRemaining()) {
							allBytes.put((byte)0xff) // Fill remaining with the sign bit
						}
						global2 = fromLittleEndianByteArray(allBytes.array())

						if (global2 <=> BigInteger.ZERO == 1) {
							logger.warn('Untested code branch in MixFileKey')
							edi--
						}
					}
				}
			}
			global2 = global2.negate().subtract(BigInteger.ONE)
		}
		return new BigInteger(global2.toString())
	}

	/**
	 * No damn idea as to what this does except to look confusing as hell.  Looks
	 * to be some modification of the current position of the buffer against the
	 * global inverse lo/hi values.
	 * 
	 * @param values
	 * @return
	 */
	private int getMulShort(ShortBuffer values) {

		def i = (((((((((values.get(values.position() - 1) ^ 0xffff) & 0xffff) * g1hiinvlo + 0x10000) >>> 1) +
			((((values.get(values.position() - 2) ^ 0xffff) & 0xffff) * g1hiinvhi + g1hiinvhi) >>> 1) + 1) >>> 16) +
			((((values.get(values.position() - 1) ^ 0xffff) & 0xffff) * g1hiinvhi) >>> 1) +
			((((values.get(values.position()) ^ 0xffff) & 0xffff) * g1hiinvlo) >>> 1) + 1) >>> 14) + g1hiinvhi *
			((values.get(values.position()) ^ 0xffff) & 0xffff) * 2) >>> g1hibitlength

		if (i > 0xffff) {
			i = 0xffff
		}
		return i
	}

	/**
	 * Sets all of the temporary variables back to 0.
	 */
	void clearTempVars() {

		global1 = BigInteger.ZERO
		g1lengthx2 = 0

		g1hi = BigInteger.ZERO
		g1hiinv = BigInteger.ZERO
		g1hibitlength = 0
		g1hiinvlo = 0
		g1hiinvhi = 0

		global2 = BigInteger.ZERO
	}
}
