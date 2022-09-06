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

package nz.net.ultraq.redhorizon.classic.filetypes

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.nio.ByteBuffer
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

	static final int SIZE_KEY_SOURCE = 80
	static final int SIZE_KEY = 56

	// TODO: The key values are constant, so maybe just save those instead
	//       of running the calculations every time?
	// A base64 encoded string of the public key used for the transformation function
	static final String PUBLIC_KEY_STRING = 'AihRvNoIbTn85FZRYNZRcT+i6KpU+maCsEqr3Q5q+LDB5tH7Tz2qQ38V'

	private static BigInteger publicKey
	private static int pkHiBitLength
	private static int pkHiInvLo
	private static int pkHiInvHi

	static {

		// Construct the public key bigint from the public key string
		// -------------------------------------------------------------------------

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


		// Initializes several temporary variables derived from the public key
		// -------------------------------------------------------------------------

		def pkHiInt = new BigInteger(publicKey.toByteArray(), 0, 4)
		pkHiBitLength = pkHiInt.bitLength() % 16
		def pkHiIntInv = (bigNumberInverse(pkHiInt << (32 - pkHiInt.bitLength())) >> 1) + 1
		if (pkHiIntInv.bitLength() > 32) {
			pkHiIntInv >>= 1
			pkHiBitLength--
		}
		def g1hiinvAsShortBuffer = ByteBuffer.wrap(pkHiIntInv.toByteArrayNoSignByte()).asShortBuffer()
		pkHiInvHi = g1hiinvAsShortBuffer.get() & 0xffff
		pkHiInvLo = g1hiinvAsShortBuffer.get() & 0xffff
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
	private static BigInteger bigNumberInverse(BigInteger value) {

		// For the resulting binary value
		def dest = new byte[16]
		def destPos = value.byteLength()

		// A rotating bit mask
		def bitLength = value.bitLength()
		def bit = 1 << (bitLength % 8)

		// ???
		def temp = new BigInteger(ByteBuffer.allocateNative(16)
			.put(value.byteLength() - 1, 1 << (((bitLength - 1) % 8) & 0x1f) as byte)
			.array()
			.reverse())

		while (bitLength--) {
			temp <<= 1
			if (temp <=> value != -1) {
				temp -= value
				dest[destPos] |= bit
			}
			bit >>>= 1
			if (!bit) {
				destPos--
				bit = 0x80
			}
		}

		return new BigInteger(dest.reverse())
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

		// Process the key in parts of equal size to the public key
		source.split(publicKey.byteLength()).each { sourcePart ->
			def sourceValue = new BigInteger(sourcePart.array().reverse())

			// Recursive function to reduce the partial source value through multiple
			// rounds of the {@link #calculateKeyBigNumber(BigInteger, BigInteger)}
			// method, returning the partial key.
			def calculateKeyBigNumberRecursive = null
			calculateKeyBigNumberRecursive = { BigInteger value, int rounds ->
				return rounds ?
					calculateKeyBigNumberRecursive(calculateKeyBigNumber(value, value), rounds - 1) :
					value
			}
			def partialKey = calculateKeyBigNumber(
				calculateKeyBigNumberRecursive(new BigInteger(sourceValue.toString()), 16),
				sourceValue)

			key.put(partialKey.toByteArrayNoSignByte().reverse())
		}

		return key.rewind()
	}

	/**
	 * Further calculation of the key based on the given 2 sources values.
	 * <p>
	 * This method had some rather complicated C++ code, so I've mostly kept it
	 * intact and translated it to Java/Groovy to keep it working.
	 * 
	 * @param source1
	 * @param source2
	 * @return Key value calculated from the 2 sources.
	 */
	private BigInteger calculateKeyBigNumber(BigInteger source1, BigInteger source2) {

		def partialKey = source1 * source2

		if (partialKey.shortLength() >= publicKey.shortLength()) {
			partialKey = -(partialKey + 1)

			// The following loop operates on as section of the partial key's bytes,
			// delimited by the "lengthDiff" value, as if it were a short value array.
			// It slowly reduces the partial key value to the size of the public key.

			def lengthDiff = partialKey.shortLength() + 1 - publicKey.shortLength()
			def partialKeyBytes = asNativeBigInteger(partialKey)
			def partialKeyShorts = partialKeyBytes.asShortBuffer()
			partialKeyShorts.position(partialKeyShorts.limit() - 2) // To position it at the int-sized sign value 

			while (lengthDiff--) {
				def offset = lengthDiff * 2
				def leRange = partialKeyBytes.limit() - offset

				def temp = calculateKeyShort(partialKeyShorts)
				if (temp > 0) {
					def partial = forRange(partialKeyBytes, offset, leRange) { rangeValue ->
						return publicKey * new BigInteger(Integer.toString(temp)) + rangeValue
					}

					if (partial.signum() != -1) {
						forRange(partialKeyBytes, offset, leRange) { rangeValue ->
							return rangeValue - publicKey
						}
					}
				}
				partialKeyShorts.advance(-1)
			}
			partialKey = -new BigInteger(partialKeyBytes.array().reverse()) - 1
		}

		return new BigInteger(partialKey.toString())
	}

	/**
	 * No damn idea as to what this does except to look confusing as hell.  Looks
	 * to be some modification of the current position of the buffer against the
	 * public key inverse lo/hi values.
	 * 
	 * @param values
	 * @return
	 */
	private int calculateKeyShort(ShortBuffer values) {

		def i = (((((((((values.get(values.position() - 1) ^ 0xffff) & 0xffff) * pkHiInvLo + 0x10000) >>> 1) +
			((((values.get(values.position() - 2) ^ 0xffff) & 0xffff) * pkHiInvHi + pkHiInvHi) >>> 1) + 1) >>> 16) +
			((((values.get(values.position() - 1) ^ 0xffff) & 0xffff) * pkHiInvHi) >>> 1) +
			((((values.get(values.position()) ^ 0xffff) & 0xffff) * pkHiInvLo) >>> 1) + 1) >>> 14) + pkHiInvHi *
			((values.get(values.position()) ^ 0xffff) & 0xffff) * 2) >>> pkHiBitLength

		if (i > 0xffff) {
			i = 0xffff
		}
		return i
	}

	/**
	 * Convert a {@code BigInteger} to its byte representation equivalent as in
	 * the original C++ code, which is a little endian byte array filled to the
	 * nearest int size, padding with sign bytes if necessary.
	 * 
	 * @param source
	 * @return
	 */
	private static ByteBuffer asNativeBigInteger(BigInteger source) {

		def sourceBytes = source.toByteArray().reverse()
		def sourceLength = sourceBytes.length
		if (sourceLength % 4) {
			sourceLength += 4 - sourceLength % 4
		}
		def sourceBuffer = ByteBuffer.allocateNative(sourceLength + 4)
			.put(sourceBytes)
		while (sourceBuffer.hasRemaining()) {
			sourceBuffer.put((byte)(source.signum() == -1 ? -1 : 0))
		}
		return sourceBuffer.rewind()
	}

	/**
	 * Perform {@code BigInteger} calculations using byte data from a section of a
	 * buffer.  The bytes comprising the section will be passed to the given
	 * closure as a {@code BigInteger}, and the returned result will then be used
	 * to replace those bytes, padding out any remaining space with the sign byte.
	 * 
	 * @param buffer
	 * @param offset
	 * @param length
	 * @param closure
	 * @return
	 */
	private static BigInteger forRange(ByteBuffer buffer, int offset, int length,
		@ClosureParams(value = SimpleType, options = 'java.math.BigInteger') Closure<BigInteger> closure) {

		def rangeBytes = new byte[length]
		System.arraycopy(buffer.array(), offset, rangeBytes, 0, length)
		def result = closure(new BigInteger(rangeBytes.reverse()))
		buffer
			.position(offset)
			.put(result.toByteArray().reverse())
		while (buffer.hasRemaining()) {
			buffer.put((byte)(result.signum() == -1 ? -1 : 0))
		}
		buffer.rewind()
		return result
	}
}
