/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.filetypes.mix

import groovy.transform.PackageScope
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * A MIX file specific to the encrypted format found in the Red Alert game.
 * Used as a delegate for {@link MixFile}.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
class MixFileDelegateEncrypted extends MixFileDelegate {

	private static final int SIZE_ENCRYPTED_BLOCK = 8
	private static final int SIZE_FLAG = 4

	final short numEntries
	final int dataSize
	final MixEntry[] entries
	final int baseEntryOffset

	/**
	 * Constructor, start building out a MIX file using the spec from the Red
	 * Alert game.
	 * 
	 * @param input
	 */
	MixFileDelegateEncrypted(DataInput input) {

		// Retrieve the Blowfish key used for decrypting the header and file entry index
		def keySource = ByteBuffer.wrapNative(input.readNBytes(MixFileKey.SIZE_KEY_SOURCE))
		def key = new MixFileKey().calculateKey(keySource)
		def blowfishSecretKey = new SecretKeySpec(key.array(), 'Blowfish')
		def blowfishCipher = Cipher.getInstance('Blowfish/ECB/NoPadding')
		blowfishCipher.init(Cipher.DECRYPT_MODE, blowfishSecretKey)

		// Decrypt the first block to obtain the header
		def headerEncryptedBytes = input.readNBytes(SIZE_ENCRYPTED_BLOCK)
		def headerDecryptedBuffer = ByteBuffer.allocateNative(SIZE_ENCRYPTED_BLOCK)
		blowfishCipher.doFinal(headerEncryptedBytes, 0, headerEncryptedBytes.length, headerDecryptedBuffer.array(), 0)

		numEntries = headerDecryptedBuffer.getShort()
		dataSize = headerDecryptedBuffer.getInt()

		// Knowing the number of entries ahead, decrypt as many 8 byte blocks that
		// fit the index, reading it and the 2 unread bytes from the first block
		def numBytesForIndex = (int)Math.ceil((MixEntry.SIZE * numEntries) / SIZE_ENCRYPTED_BLOCK) * 8
		def indexEncryptedBytes = input.readNBytes(numBytesForIndex)
		def indexDecryptedBuffer = ByteBuffer.allocateNative(numBytesForIndex)
		blowfishCipher.doFinal(indexEncryptedBytes, 0, indexEncryptedBytes.length, indexDecryptedBuffer.array(), 0)

		def decryptedIndexBuffer = ByteBuffer.allocateNative(numBytesForIndex + 2)
			.put(headerDecryptedBuffer)
			.put(indexDecryptedBuffer)
			.rewind()
		entries = new MixEntry[numEntries]
		numEntries.times { index ->
			entries[index] = new MixEntry(decryptedIndexBuffer)
		}

		baseEntryOffset = SIZE_FLAG + MixFileKey.SIZE_KEY_SOURCE + SIZE_ENCRYPTED_BLOCK + numBytesForIndex
	}
}
