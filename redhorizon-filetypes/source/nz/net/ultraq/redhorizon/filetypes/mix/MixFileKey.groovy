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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
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
	 * Convert a little endian byte array to a {@code BigInteger}.
	 * 
	 * @param bytes
	 * @return
	 */
	private static BigInteger fromLittleEndianByteArray(byte[] bytes) {

		return new BigInteger(bytes.reverse())
	}

	/**
	 * Convert a {@code BigInteger}'s big endian byte representation to a little
	 * endian byte array.
	 * 
	 * @param value
	 * @return
	 */
	private static byte[] toLittleEndianByteArray(BigInteger value) {

		return value.toByteArrayNoSignByte().reverse()
	}

	/**
	 * Prints the values of the {@code BigInteger} in a similar format to how the
	 * C++ code does (as little-endian int-sized chunks).
	 * 
	 * @param name
	 * @param integer
	 */
	private static void printBigInteger(String name, BigInteger integer) {

		printByteBuffer(name, ByteBuffer.wrap(toLittleEndianByteArray(integer)).order(ByteOrder.nativeOrder()))
	}

	private static void printByteBuffer(String name, ByteBuffer buffer) {

		printf("${name} (Groovy): ")
		if (buffer.order() != ByteOrder.nativeOrder()) {
			buffer = ByteBuffer.wrap(buffer.array().reverse()).order((ByteOrder.nativeOrder()))
		}
		IntBuffer integerInts = buffer.asIntBuffer()
		for (int i = 0; i < integerInts.limit(); i++) {
			printf('0x%x, ', integerInts.get(i))
		}
		int remainder = buffer.limit() % 4;
		if (remainder != 0) {
			int remaining = 0
			for (int i = 0; i < remainder; i++) {
				remaining <<= 8
				remaining |= buffer.get(buffer.limit() - 1 - i) & 0xff
			}
			printf('0x%x, ', remaining)
		}
		println()
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
		def processingSize = publicKey.byteLength()
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

		def key = new BigInteger(source.toString())
		16.times {
			key = calculateKeyBigNumber(key, key)
		}
		key = calculateKeyBigNumber(key, source)

		return key
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

		// The original C++ code worked on byte arrays, working on them as ints and
		// doing BigNumber arithmetic, so what follows is pretty nuts.

		def tempBytes = ByteBuffer.allocate(256).order(ByteOrder.nativeOrder())
		def tempInts = tempBytes.asIntBuffer()
		def dest = new int[4]
		def destPos = 0

		def bitLength = value.bitLength()
		def bit = 1 << (bitLength % 32)
		destPos += value.intLengthNoSign()
		def intLength = value.intLengthNoSign()
		tempInts.put(intLength / 4 as int, tempInts.get(tempInts.position()) | (1 << ((bitLength - 1) & 0x1f)))

		def temp = fromLittleEndianByteArray(tempBytes.array())

		while (bitLength--) {
			temp <<= 1
			if (temp <=> value != -1) {
				temp -= value
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

		def g1lengthx2 = publicKey.shortLength()
		def key = source1 * source2

		def g2lengthx2 = key.shortLength()
		if (g2lengthx2 >= g1lengthx2) {
			key = (key + 1).negate()

			def lengthdiff = g2lengthx2 + 1 - g1lengthx2

			def global2Bytes = asNativeBigInteger(key)
			def global2Shorts = global2Bytes.asShortBuffer()
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
					def partial = copy(key, esi * 2, key.byteLength() - (esi * 2))
					partial = publicKey * new BigInteger(Integer.toString(temp)) + partial

					key = replace(key, partial, esi * 2)
					global2Bytes = asNativeBigInteger(key)
					global2Shorts = global2Bytes.asShortBuffer()

					printBigInteger("global2 after", key)
					if (edi < global2Shorts.limit() && (global2Shorts.get(edi) & 0x8000) == 0) {

						// TODO: Another split calculation
						def partial2 = copy(key, esi * 2, key.byteLength() - (esi * 2))
						partial2 -= publicKey

						key = replace(key, partial2, esi * 2)
						global2Bytes = asNativeBigInteger(key)
						global2Shorts = global2Bytes.asShortBuffer()

						printBigInteger('global2 after subtract', key)
						if (key <=> 0) {
							println('Positive?')
							edi--
						}
					}
				}
			}
			key = key.negate() - 1
		}
		return new BigInteger(key.toString())
	}

	/**
	 * Convert a {@code BigInteger} to its byte representation equivalent as in
	 * the original C++ code, which is a little endian byte array with sign bytes.
	 * 
	 * @param source
	 * @return
	 */
	private static ByteBuffer asNativeBigInteger(BigInteger source) {

		def sourceBytes = toLittleEndianByteArray(source)
		return ByteBuffer.allocateNative(sourceBytes.length + 4)
			.put(sourceBytes)
			.putInt(source.signum() == -1 ? -1 : 0) // Mock sign int
			.rewind()
	}

	/**
	 * Use {@code num} bytes from the byte value of the {@code BigInteger} to
	 * create a new {@code BigInteger}.
	 * 
	 * @param source
	 * @param offset
	 * @param length
	 * @return
	 */
	private static BigInteger copy(BigInteger source, int offset, int length) {

		def sourceBytes = source.toByteArrayNoSignByte()
		def sourceBuffer = ByteBuffer.allocate(sourceBytes.length + 4)
			.putInt(source.signum() == -1 ? -1 : 0) // Mock sign int
			.put(sourceBytes)
		return new BigInteger(sourceBuffer.array(), sourceBuffer.limit() - length - offset - 4, length + 4)
	}

	private static ByteBuffer fill(ByteBuffer buffer, byte val) {

		buffer.rewind()
		while (buffer.hasRemaining()) {
			buffer.put(val)
		}
		return buffer.rewind()
	}

	/**
	 * Replace {@code num} bytes of the bytes in {@code source} with those from
	 * {@code replaceWith}.
	 * 
	 * @param source
	 * @param replaceWith
	 * @param retain
	 * @return
	 */
	private static BigInteger replace(BigInteger source, BigInteger replaceWith, int retain) {

		// TODO: Since this operation works with the method above, it can probably
		//       be contained within a closure.
		def sourceBytes = source.toByteArrayNoSignByte()
		def replaceBytes = replaceWith.toByteArrayNoSignByte()

		def sourceBuffer = fill(ByteBuffer.allocate(sourceBytes.length + 4), (byte)(source.signum() == -1 ? -1 : 0))
		sourceBuffer.position(sourceBuffer.limit() - retain - replaceBytes.length - 4)
		sourceBuffer.putInt(replaceWith.signum() == -1 ? -1 : 0)

		sourceBuffer.put(replaceBytes)
		sourceBuffer.put(sourceBytes, sourceBuffer.position() - 4, sourceBuffer.remaining())

		return new BigInteger(sourceBuffer.array())
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
}
