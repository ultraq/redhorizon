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

package nz.net.ultraq.redhorizon.filetypes.mix;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Base64;

/**
 * Interface class for the native C++ functions used to obtain the 56-byte
 * Blowfish key from the 80-byte key source found in encrypted MIX files.
 * 
 * @author Emanuel Rabina
 */
public class MixFileKey {

	static {
		System.loadLibrary("MixFileKey");
	}

	// TODO: The public key values are constant, so maybe just save those instead
	//       of running the calculations every time?
	private static final String keyString = "AihRvNoIbTn85FZRYNZRcT+i6KpU+maCsEqr3Q5q+LDB5tH7Tz2qQ38V";

	private static class PublicKey {
		BigInteger key1;
		BigInteger key2;
		int length;
	}
	private static PublicKey publicKey;

	private static BigInteger global1;
	static int g1bitlength;
	static int g1lengthx2;

	static BigInteger g1hi;
	static BigInteger g1hiinv;
	static int g1hibitlength;
	static int g1hiinvlo;
	static int g1hiinvhi;

	static BigInteger global2;

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private MixFileKey() {
	}

	/**
	 * Convert a little endian byte array to a {@code BigInteger}.
	 * 
	 * @param bytes
	 * @return
	 */
	private static BigInteger fromLittleEndianByteArray(byte[] bytes) {

		return fromLittleEndianByteArray(bytes, 0, bytes.length);
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

		byte[] flipped = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			flipped[i] = bytes[bytes.length - 1 - i];
		}
		return new BigInteger(flipped, flipped.length - length - offset, length);
	}

	/**
	 * Convert a {@code BigInteger}'s big endian byte representation to a little
	 * endian byte array.
	 * 
	 * @param value
	 * @return
	 */
	private static byte[] toLittleEndianByteArray(BigInteger value) {

		byte[] bytes = value.toByteArray();

		// Strip out any bytes representing a sign bit
		if (bytes[0] == 0) {
			byte[] newBytes = new byte[bytes.length - 1];
			System.arraycopy(bytes, 1, newBytes, 0, newBytes.length);
			bytes = newBytes;
		}

		byte[] flipped = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			flipped[i] = bytes[bytes.length - 1 - i];
		}
		return flipped;
	}

	/**
	 * Prints the values of the {@code BigInteger} in a similar format to how the
	 * C++ code does (as little-endian int-sized chunks).
	 * 
	 * @param name
	 * @param integer
	 */
	static void printBigInteger(String name, BigInteger integer) {

		System.out.print(name + " (Java): ");
		ByteBuffer integerBytes = ByteBuffer.wrap(toLittleEndianByteArray(integer)).order(ByteOrder.nativeOrder());
		IntBuffer integerInts = integerBytes.asIntBuffer();
		for (int i = 0; i < integerInts.limit(); i++) {
			System.out.print(String.format("0x%x, ", integerInts.get(i)));
		}
		int remainder = integerBytes.limit() % 4;
		if (remainder != 0) {
			int remaining = 0;
			for (int i = 0; i < remainder; i++) {
				remaining <<= 8;
				remaining |= integerBytes.get(integerBytes.limit() - 1 - i) & 0xff;
			}
			System.out.print(String.format("0x%x, ", remaining));
		}
		System.out.println();
	}

	static void printByteBuffer(String name, ByteBuffer buffer) {

		System.out.print(name);
//		IntBuffer integerInts = buffer.asIntBuffer();
//		for (int i = 0; i < integerInts.limit(); i++) {
//			System.out.print(String.format("0x%x, ", integerInts.get(i)));
//		}
		while (buffer.hasRemaining()) {
			System.out.print(String.format("0x%x, ", buffer.get() & 0xff));
		}
		buffer.rewind();
		System.out.println();
	}

	/**
	 * Calculates the 56-byte Blowfish key from the 80-byte key source found in
	 * Red Alert's MIX files.
	 * 
	 * @param source A buffer containing the 80-byte key source.
	 * @param dest   A buffer store for the 56-byte Blowfish key.
	 */
	static void getBlowfishKey(ByteBuffer source, ByteBuffer dest) {

		if (publicKey == null) {
			initPublicKey();
		}

		// The C++ code expects key1 to be in little endian format, but Java's
		// BitIntegers are in big endian, so flip that one around here for now.
		byte[] key1BytesFlipped = toLittleEndianByteArray(publicKey.key1);

		printByteBuffer("Source (Java): ", source);
		ByteBuffer key = ByteBuffer.allocate(56).order(ByteOrder.nativeOrder());
		predataProcessing(source, predataLength(), key);
		source.rewind();
		printByteBuffer("Key (Java): ", key);

		dest.put(key).flip();

//		System.out.println();
//		getBlowfishKeyNative(source.array(), dest.array(),
//			key1BytesFlipped, publicKey.key2.toByteArray(), publicKey.length);
//		printByteBuffer("Key (C++): ", dest);
//		System.out.println();
	}

	/**
	 * Initializes the variables in the PublicKey struct.  Namely, fills the
	 * PublicKey.Key1 BigNumber with what appears to be a list of characters based
	 * from the keystring variable, and the character map.
	 */
	private static void initPublicKey() {

		publicKey = new PublicKey();
		publicKey.key2 = new BigInteger([0x00, 0x01, 0x00, 0x01] as byte[]);

		ByteBuffer decodedKeyString = ByteBuffer.wrap(Base64.getDecoder().decode(keyString));
		int keyLength = dataLength(decodedKeyString);

		publicKey.key1 = moveDataToKey(decodedKeyString, keyLength);
		publicKey.length = publicKey.key1.bitLength() - 1; // Should be 318 from C++

// From Java -> C
// Public key 2: 0x10001, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 
// Public key 1: 0x8dabc51, 0xe4fc396d, 0xd6605156, 0xa23f7151, 0xfa54aae8, 0x4ab08266, 0x6a0eddab, 0xe6c1b0f8, 0x3d4ffbd1, 0x157f43aa, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x10001, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,

// From C
// Public key 2: 0x10001, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 
// Public key 1: 0xaa437f15, 0xd1fb4f3d, 0xf8b0c1e6, 0xabdd0e6a, 0x6682b04a, 0xe8aa54fa, 0x51713fa2, 0x565160d6, 0x6d39fce4, 0x51bcda08, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x10001, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 

// 42 bytes long
// 2, 40, 81, -68, -38, 8, 109, 57, -4, -28, 86, 81, 96, -42, 81, 113, 63, -94, -24, -86, 84, -6, 102, -126, -80, 74, -85, -35, 14, 106, -8, -80, -63, -26, -47, -5, 79, 61, -86, 67, 127, 21
// 0x2, 0x28, 0x51, 0xbc, 0xda, 0x8, 0x6d, 0x39, 0xfc, 0xe4, 0x56, 0x51, 0x60, 0xd6, 0x51, 0x71, 0x3f, 0xa2, 0xe8, 0xaa, 0x54, 0xfa, 0x66, 0x82, 0xb0, 0x4a, 0xab, 0xdd, 0xe, 0x6a, 0xf8, 0xb0, 0xc1, 0xe6, 0xd1, 0xfb, 0x4f, 0x3d, 0xaa, 0x43, 0x7f, 0x15
	}

	/**
	 * Attempts to discover the length of the key (in bytes) from the key string.
	 * This is normally found in the 1st index position (data[1]) of the string.
	 * But if the value at that position has it's sign bit set to 1, then discover
	 * the length using a loop.
	 * 
	 * Once the length has been determined, a call moveDataToKey() is made to
	 * begin moving bytes over.
	 * 
	 * @param data Base64 decoded key string.
	 * @return The data length value from the decoded string. 
	 */
	private static int dataLength(ByteBuffer data) {

		// NOTE: Is the first byte always "2"?
		if (data.get() != 2) {
			return 0;
		}

		int keyLength;
		if ((data.get(0) & 0x80) != 0) {
			keyLength = 0;
			for (int i = 0; i < (data.get(0) & 0x7f); i++) {
				keyLength = (keyLength << 8) | data.get(i + 1);
			}
			data.position(data.position() + (data.get(0) & 0x7f) + 1);
		}
		else {
			keyLength = data.get();
		}
		data.rewind();

		return keyLength; // Should be 40 from C++
	}

	/**
	 * Transfers byte data from the key string to the key BigNumber.
	 * 
	 * @param data      Base64 decoded key string.
	 * @param keyLength Byte length of the data.
	 */
	private static BigInteger moveDataToKey(ByteBuffer data, int keyLength) {

		byte[] keyVal = new byte[keyLength];
		System.arraycopy(data.array(), 2, keyVal, 0, keyLength);
		return new BigInteger(keyVal);
	}

	/**
	 * The length of the byte values going into the predataProcessing() method?
	 * Not too sure, although it does return some factor of 80 (40 on most
	 * occassions, so maybe the number of times to perform key calculations in
	 * the predataProcessing() method?)
	 *
	 * @return The some value related to the length of the public key.
	 */
	static int predataLength() {

//		int a = (publicKey.length - 1) / 8;
//		int result = (55 / a + 1) * (a + 1);
//		System.out.println("Result: " + result);
//		return result;
		return 80; // Can be constant since it's based off public key which is also constant?
		           // Does this have anything to do with the source being 80 bytes in length?
	}

	/**
	 * Initializes some temporary BigNumber variables to be put through the various
	 * private key functions/calculations, which then get copied to the destination
	 * byte buffer for the Blowfish key.
	 * 
	 * @param source The 80-byte key source.
	 * @param dest   Byte buffer for the 56-byte Blowfish key.
	 */
	static void predataProcessing(ByteBuffer source, int predatalength, ByteBuffer dest) {

//		BigInteger n2, n3;

		int a = (publicKey.length - 1) / 8;
		while (a + 1 <= predatalength) {

//			initBigNumber(n2, 0, 64);
//			memmove(n2, source, a + 1);
			byte[] sourcePart = new byte[a + 1];
			source.get(sourcePart);
			BigInteger n2 = fromLittleEndianByteArray(sourcePart);

//			calculateKey(n3, n2, PublicKey.key2, PublicKey.key1, 64);
			BigInteger keyPart = calculateKey(n2, publicKey.key2, publicKey.key1);

//			memmove(dest, n3, a);
			byte[] keyArray = toLittleEndianByteArray(keyPart);
			dest.put(keyArray, 0, keyArray.length);

			predatalength -= a + 1;
//			source += a + 1;
//			dest += a;
//			source.position(source.position() + a + 1);
//			dest.position(dest.position() + a);
		}
		dest.flip();
	}

	/**
	 * Performs calculations on the public key to generate the Blowfish key.  Most
	 * of it seems to get off-loaded to the calculateBigNumber() method though.
	 * 
	 * @param n2 -
	 * @param n3 -
	 * @param n4 -
	 */
	static BigInteger calculateKey(BigInteger n2, BigInteger n3, BigInteger n4) {

		BigInteger temp;

//		initBigNumber(n1, 1, limit);

//		int n4length = bignumberIntLength(n4, limit);
		int n4Length = (n4.bitLength() + 31) >> 5; // The int length of the big number
		initTwoInts(n4);

		int n3bitlength = n3.bitLength();
		int n3length = (n3bitlength + 31) >> 5;

//		unsigned int bitmask = (((unsigned int )1) << ((n3bitlength - 1) % 32)) >> 1;
		int bitmask = (1 << ((n3bitlength - 1) % 32)) >> 1;

//		n3 += n3length - 1;
		ByteBuffer n3Bytes = ByteBuffer.wrap(toLittleEndianByteArray(n3)).order(ByteOrder.nativeOrder());
		n3Bytes.position(n3Bytes.limit() - 1);
		n3bitlength--;

//		BigInteger n1 = bignumberMove(n2);
		BigInteger n1 = new BigInteger(n2.toString());

		while (--n3bitlength >= 0) {

			if (bitmask == 0) {
				bitmask = 0x80000000;
				n3Bytes.position(n3Bytes.position() - 1);
			}
			temp = calculateBigNumber(n1, n1);
//			printBigInteger("temp", temp);

//			if (*n3 & bitmask) {
//				calculateBigNumber(n1, temp, n2, n4Length);
//			}
//			else {
//				bignumberMove(n1, temp, n4Length);
//			}
//			bitmask >>= 1;

			if ((n3Bytes.get(n3Bytes.position()) & bitmask) != 0) {
				n1 = calculateBigNumber(temp, n2);
			}
			else {
				n1 = new BigInteger(temp.toString());
			}
			bitmask >>>= 1;
		}
//		initBigNumber(temp, 0, n4Length);
//		clearTempVars(limit);
		temp = BigInteger.ZERO;
		clearTempVars();

		printBigInteger("n1", n1);
		return n1;
	}

	/**
	 * Not too sure on this method, but it looks as if it just initializes the
	 * several temporary variables with some meaningful values.
	 * 
	 * @param bignum A BigNumber to source all the values from.
	 */
	static void initTwoInts(BigInteger bignum) {

//		bignumberMove(global1, bignum, limit);
//		g1bitlength = bignumberBitLength(global1, limit);
//		g1lengthx2 = (g1bitlength + 15) / 16;
		global1 = new BigInteger(bignum.toString());
		g1bitlength = global1.bitLength();
		g1lengthx2 = (g1bitlength + 15) / 16; // Length as a short???

//		bignumberMove(g1hi, global1 + bignumberIntLength(global1, limit) - 2, 2);
//		g1hibitlength = bignumberBitLength(g1hi, 2) - 32;
		// Uses the move as a small copy
		byte[] g1hiBytes = new byte[16];
		System.arraycopy(global1.toByteArray(), 0, g1hiBytes, 0, 8);
		g1hi = new BigInteger(g1hiBytes);
//		g1hi = new BigInteger(global1.toByteArray(), 0, 8);
		g1hibitlength = g1hi.bitLength() - 32;

//		printBigInteger("g1hi", g1hi);
//		System.out.println("g1hibitlength: " + g1hibitlength); // C++ reports this as 31, Java 63 o_o

//		bignumberShiftRight(g1hi, g1hibitlength, 2);
//		bignumberInverse(g1hiinv, g1hi, 2);
//		bignumberShiftRight(g1hiinv, 1, 2);

		// Should right shift as many bits as it contains so that it ends up in the lower half
		g1hi = g1hi.shiftRight(g1hibitlength);
		g1hiinv = bigNumberInverse(g1hi);
		g1hiinv = g1hiinv.shiftRight(1);

		g1hibitlength = (g1hibitlength + 15) % 16 + 1;
//		bignumberIncrement(g1hiinv, 2);
		g1hiinv = g1hiinv.add(BigInteger.ONE);

//		if (bignumberBitLength(g1hiinv, 2) > 32) {
//			bignumberShiftRight(g1hiinv, 1, 2);
//			g1hibitlength--;
//		}
//		g1hiinvlo = *(unsigned short *)g1hiinv;
//		g1hiinvhi = *(((unsigned short *)g1hiinv) + 1);

		if (g1hiinv.bitLength() > 32) {
			g1hiinv = g1hiinv.shiftRight(1);
			g1hibitlength--;
		}
		// By this point, g1hiinv is reduced to a 32 bit value, but there may be 5
		// bytes in the array to represent leading 0s (sign bits)
		byte[] g1hiinvBytes = g1hiinv.toByteArray();
		int length = g1hiinvBytes.length;

		g1hiinvlo = (g1hiinvBytes[length - 2] & 0xff) << 8 | (g1hiinvBytes[length - 1] & 0xff);
		g1hiinvhi = (g1hiinvBytes[length - 4] & 0xff) << 8 | (g1hiinvBytes[length - 3] & 0xff);
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
	static BigInteger bigNumberInverse(BigInteger value) {

		// The original C++ code worked on byte arrays, working on them as ints and
		// doing BigNumber arithmetic, so the below is pretty nuts.

		ByteBuffer tempBytes = ByteBuffer.allocate(256).order(ByteOrder.nativeOrder());
		IntBuffer tempInts = tempBytes.asIntBuffer();
		int[] dest = new int[4];
		int destPos = 0;

		int bitLength = value.bitLength();
		int bit = 1 << (bitLength % 32);
		destPos += ((bitLength + 32) / 32) - 1;
		int byteLength = ((bitLength - 1) / 32 as int) * 4;
		tempInts.put(byteLength / 4 as int, tempInts.get(tempInts.position()) | (1 << ((bitLength - 1) & 0x1f)));

		BigInteger temp = fromLittleEndianByteArray(tempBytes.array());

		while (bitLength-- > 0) {
			temp = temp.shiftLeft(1);
			if (temp.compareTo(value) != -1) {
				temp = temp.subtract(value);
				dest[destPos] |= bit;
			}
			bit >>>= 1;
			if (bit == 0) {
				destPos--;
				bit = 0x80000000;
			}
		}

		byte[] destBytes = new byte[dest.length * 4];
		for (int i = 0; i < destBytes.length; i++) {
			destBytes[i] = (byte)(dest[i / 4 as int] >>> (8 * (i % 4)));
		}

		return fromLittleEndianByteArray(destBytes);
	}

	/**
	 * Calculates a BigNumber?  It seems to be a further step to the overall key
	 * calculation, but I'm not sure what it does.
	 * 
	 * @param n2 -
	 * @param n3 -
	 */
	static BigInteger calculateBigNumber(BigInteger n2, BigInteger n3) {

//		bignumberMultiply(global2, n2, n3);
//		global2[limit * 2] = 0;
		global2 = n2.multiply(n3);

//		int g2lengthx2 = bignumberIntLength(global2, limit * 2 + 1) * 2;
		int g2lengthx2 = ((global2.bitLength() + 31) / 32 as int) * 2; // the integer length * 2
		if (g2lengthx2 >= g1lengthx2) {
//			bignumberIncrement(global2, limit * 2 + 1);
//			bignumberNegate(global2, limit * 2 + 1);
			global2 = global2.add(BigInteger.ONE);
			global2 = global2.negate();

			int lengthdiff = g2lengthx2 + 1 - g1lengthx2;

//			unsigned short *esi = ((unsigned short *)global2) + (1 + g2lengthx2 - g1lengthx2);
//			unsigned short *edi = ((unsigned short *)global2) + (g2lengthx2 + 1);
			byte[] global2RawBytes = toLittleEndianByteArray(global2);
			ByteBuffer global2Bytes;
			if (global2.compareTo(BigInteger.ZERO) == -1) {
				global2Bytes = ByteBuffer.allocate(global2RawBytes.length + 4)
					.order(ByteOrder.nativeOrder())
					.put(global2RawBytes)
					.putInt(-1) // Mock sign int
					.rewind();
			}
			else {
				global2Bytes = ByteBuffer.wrap(global2RawBytes)
					.order(ByteOrder.nativeOrder());
			}
			ShortBuffer global2Shorts = global2Bytes.asShortBuffer();
			int esi = lengthdiff;
			int edi = g2lengthx2 + 1;

			for (; lengthdiff > 0; lengthdiff--) {
				esi--;
				edi--;
//				unsigned short temp = getMulShort((unsigned int *)edi);
				int temp = getMulShort(global2Shorts.position(edi));

				if (temp > 0) {
//					bignumberMultiplyWord((unsigned int *)esi, global1, temp, limit * 2);
//					global2 = global1.multiply(new BigInteger(Integer.toString(temp)));
//					System.out.println(String.format("esi pointer at 0x%x", global2Shorts.get(esi)));
//					printBigInteger("global2", global2);
//					BigInteger partial = new BigInteger(global2Bytes.array(), esi * 2, global2Bytes.array().length - (esi * 2));
					byte[] global2BytesArray = global2Bytes.array();
					BigInteger partial = fromLittleEndianByteArray(global2BytesArray, esi * 2, global2BytesArray.length - (esi * 2));
//					printBigInteger("partial", partial);
//					partial = bigNumberMultiplyWord(partial, global1, temp);
//					printBigInteger("global2 before", global2);
					partial = global1.multiply(new BigInteger(Integer.toString(temp))).add(partial);
//					printBigInteger("partial", partial);

					// Reconstruct the value
					ByteBuffer allBytes = global2Bytes.duplicate();
					byte[] partialArray = toLittleEndianByteArray(partial);
					allBytes.position(esi * 2).put(partialArray);
					while (allBytes.hasRemaining()) {
						allBytes.put((byte)0xff); // Fill remaining with the sign bit
					}
					global2 = fromLittleEndianByteArray(allBytes.array());
//					printBigInteger("global2 after", global2);

//global2 before (Java): 0x970a8fce, 0xb9aeaafc, 0x3809870e, 0xe75ea534, 0x5e13b4bf, 0x19048422, 0xb5f1482e, 0x73081c7e, 0xe782c193, 0xc5db6751, 0x47f12cc6, 0x1b4091d7, 0xd29e0422, 0xcfd75f8c, 0x69b7e64b, 0x3a7f5d1c, 0x8857aa4b, 0xa35c544f, 0x88b7e2a4, 0xfe04336e,
//global2 after (Java):  0x970a8fce, 0xb9aeaafc, 0x3809870e, 0xe75ea534, 0x5e13b4bf, 0x19048422, 0xb5f1482e, 0x73081c7e, 0xe782c193, 0x12496751, 0x70f0abfe, 0x6adcc0b5, 0x5f029df6, 0xbf9ece84, 0x34f095fe, 0x78506f08, 0xfb757fac, 0x558c71be, 0xb90e48cd,

//global2 before:        0x970a8fce, 0xb9aeaafc, 0x3809870e, 0xe75ea534, 0x5e13b4bf, 0x19048422, 0xb5f1482e, 0x73081c7e, 0xe782c193, 0xc5db6751, 0x47f12cc6, 0x1b4091d7, 0xd29e0422, 0xcfd75f8c, 0x69b7e64b, 0x3a7f5d1c, 0x8857aa4b, 0xa35c544f, 0x88b7e2a4, 0xfe04336e, 0xffffffff, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
//global2 after:         0x970a8fce, 0xb9aeaafc, 0x3809870e, 0xe75ea534, 0x5e13b4bf, 0x19048422, 0xb5f1482e, 0x73081c7e, 0xe782c193, 0x12496751, 0x70f0abfe, 0x6adcc0b5, 0x5f029df6, 0xbf9ece84, 0x34f095fe, 0x78506f08, 0xfb757fac, 0x558c71be, 0xb90e48cd, 0xffffde60, 0xffffffff, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,


//partial (Java): 0xabfe1249, 0xc0b570f0, 0x9df66adc, 0xce845f02, 0x95febf9e, 0x6f0834f0, 0x7fac7850, 0x71befb75, 0x48cd558c, 0xde60b90e,
//global2 (Java): 0x970a8fce, 0xb9aeaafc, 0x3809870e, 0xe75ea534, 0x5e13b4bf, 0x19048422, 0xb5f1482e, 0x73081c7e, 0xe782c193, 0x12496751, 0x70f0abfe, 0x6adcc0b5, 0x5f029df6, 0xbf9ece84, 0x34f095fe, 0x78506f08, 0xfb757fac, 0x558c71be, 0xb90e48cd,

//global2:        0x970a8fce, 0xb9aeaafc, 0x3809870e, 0xe75ea534, 0x5e13b4bf, 0x19048422, 0xb5f1482e, 0x73081c7e, 0xe782c193, 0x12496751, 0x70f0abfe, 0x6adcc0b5, 0x5f029df6, 0xbf9ece84, 0x34f095fe, 0x78506f08, 0xfb757fac, 0x558c71be, 0xb90e48cd, 0xffffde60, 0xffffffff, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,

//					printBigInteger("partial", partial);
//					printBigInteger("global2", global2);

//					if ((*edi & 0x8000) == 0) {
//						if (bignumberSubtract((unsigned int *)esi, (unsigned int *)esi, global1, 0, limit)) {
//							(*edi)--;
//						}
//					}
					if ((global2Shorts.get(edi) & 0x8000) == 0) {
						BigInteger partial2 = fromLittleEndianByteArray(global2BytesArray, esi * 2, global2BytesArray.length - (esi * 2));
						partial2 = partial2.subtract(global1);

						// Reconstruct the value
						allBytes = global2Bytes.duplicate();
						byte[] partial2Array = toLittleEndianByteArray(partial2);
						allBytes.position(esi * 2).put(partial2Array);
						while (allBytes.hasRemaining()) {
							allBytes.put((byte)0xff); // Fill remaining with the sign bit
						}
						global2 = fromLittleEndianByteArray(allBytes.array());

						if (global2.compareTo(BigInteger.ZERO) == 1) {
							System.out.println("Positive?");
							printBigInteger("global2", global2);
							edi--;
						}
					}
				}
			}
//			bignumberNegate(global2, limit);
//			bignumberDecrement(global2, limit);
			global2 = global2.negate().subtract(BigInteger.ONE);
//			printBigInteger("global2", global2);
		}
//		bignumberMove(n1, global2, limit);
		return new BigInteger(global2.toString());
	}

	/**
	 * No damn idea as to what this does except to look confusing as hell.
	 */
//	static unsigned int getMulShort(BigNumber bignum) {
	static int getMulShort(ShortBuffer values) {

//		unsigned short *wn = (unsigned short *)bignum;
//		unsigned int i = (((((((((*(wn-1) ^ 0xffff) & 0xffff) * g1hiinvlo + 0x10000) >> 1)
//			+ (((*(wn-2) ^ 0xffff) * g1hiinvhi + g1hiinvhi) >> 1) + 1)
//			>> 16) + ((((*(wn-1) ^ 0xffff) & 0xffff) * g1hiinvhi) >> 1) +
//			(((*wn ^ 0xffff) * g1hiinvlo) >> 1) + 1) >> 14) + g1hiinvhi
//			* (*wn ^ 0xffff) * 2) >> g1hibitlength;
		int i = (((((((((values.get(values.position() - 1) ^ 0xffff) & 0xffff) * g1hiinvlo + 0x10000) >>> 1) +
			((((values.get(values.position() - 2) ^ 0xffff) & 0xffff) * g1hiinvhi + g1hiinvhi) >>> 1) + 1) >>> 16) +
			((((values.get(values.position() - 1) ^ 0xffff) & 0xffff) * g1hiinvhi) >>> 1) +
			((((values.get(values.position()) ^ 0xffff) & 0xffff) * g1hiinvlo) >>> 1) + 1) >>> 14) + g1hiinvhi *
			((values.get(values.position()) ^ 0xffff) & 0xffff) * 2) >>> g1hibitlength;

		if (i > 0xffff) {
			i = 0xffff;
		}
		return i;
	}

	/**
	 * Multiplies a BigNumber by an short-sized variable.  The BigNumber
	 * multiplication method breaks-up its large task by doing several of these
	 * smaller ones.
	 *
	 * @param dest    BigNumber to hold the result of the multiplication.
	 * @param source1 BigNumber to be multiplied by.
	 * @param multiplier Smaller short-sized value to multiply the BigNumber by.
	 */
	static BigInteger bigNumberMultiplyWord(BigInteger dest, BigInteger source1, int multiplier) {

		ByteBuffer destBytes = ByteBuffer.wrap(toLittleEndianByteArray(dest)).order(ByteOrder.nativeOrder());
		ShortBuffer destShorts = destBytes.asShortBuffer();
		ByteBuffer source1Bytes = ByteBuffer.wrap(toLittleEndianByteArray(source1)).order(ByteOrder.nativeOrder());
		ShortBuffer source1Shorts = source1Bytes.asShortBuffer();

		int temp = 0;

		while (destShorts.hasRemaining()) {
//			temp = source2 * (*(unsigned short *)source1) + *(unsigned short *)dest + temp;
//			*(unsigned short *)dest = temp;
			temp = multiplier * source1Shorts.get(source1Shorts.position()) + destShorts.get(destShorts.position()) + temp;
			destShorts.put(destShorts.position(), (short)temp);

//			dest = (unsigned int *)(((unsigned short *)dest) + 1);
//			source1 = (unsigned int *)(((unsigned short *)source1) + 1);
//			temp >>= 16;
			destShorts.position(destShorts.position() + 1);
			source1Shorts.position(source1Shorts.position() + 1);
			temp >>>= 16;
		}
//		*(unsigned short *)dest += temp;
		destShorts.put(destShorts.position(), (short)(destShorts.get(destShorts.position()) + temp));
		return fromLittleEndianByteArray(destBytes.array());
	}

	/**
	 * Sets all of the temporary variables back to 0.
	 */
	static void clearTempVars() {

//		initBigNumber(global1, 0, limit);
		global1 = BigInteger.ZERO;
		g1bitlength = 0;
		g1lengthx2 = 0;

//		initBigNumber(g1hi, 0, 4);
//		initBigNumber(g1hiinv, 0, 4);
		g1hi = BigInteger.ZERO;
		g1hiinv = BigInteger.ZERO;
		g1hibitlength = 0;
		g1hiinvlo = 0;
		g1hiinvhi = 0;

//		initBigNumber(global2, 0, limit);
		global2 = BigInteger.ZERO;
	}
}
