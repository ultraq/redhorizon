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

package nz.net.ultraq.redhorizon.filetypes.mix

import blowfishj.BlowfishECB

import groovy.transform.PackageScope
import java.nio.ByteBuffer

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
	private static final int SIZE_BLOWFISH_SOURCE_KEY = 80
	private static final int SIZE_BLOWFISH_KEY = 56

	final short numEntries
	final int dataSize
	final MixFileEntry[] entries
	final int baseEntryOffset

	/**
	 * Constructor, start building out a MIX file using the spec from the Red
	 * Alert game.
	 * 
	 * @param input
	 */
	MixFileDelegateEncrypted(DataInput input) {

		// Retrieve the Blowfish key used for decrypting the header and file entry index
		def keySource = ByteBuffer.allocateNative(SIZE_BLOWFISH_SOURCE_KEY)
		input.readFully(keySource.array())
		def key = ByteBuffer.allocateNative(SIZE_BLOWFISH_KEY)
		MixFileKey.getBlowfishKey(keySource, key)
		def blowfish = new BlowfishECB(key.array(), 0, key.capacity())

		// Decrypt the first block to obtain the header
		def headerEncrypted = ByteBuffer.allocateNative(SIZE_ENCRYPTED_BLOCK)
		def headerDecrypted = ByteBuffer.allocateNative(SIZE_ENCRYPTED_BLOCK)
		input.readFully(headerEncrypted.array())
		blowfish.decrypt(headerEncrypted.array(), 0, headerDecrypted.array(), 0, headerDecrypted.capacity())

		numEntries = headerDecrypted.getShort()
		dataSize = headerDecrypted.getInt()

		// Knowing the number of entries ahead, decrypt as many blocks that fit the
		// index, reading it and the 2 unread bytes from the first block
		def numBytesForIndex = (int)Math.ceil((MixFileEntry.SIZE * numEntries) / SIZE_ENCRYPTED_BLOCK) * 8
		def encryptedBuffer = ByteBuffer.allocateNative(numBytesForIndex)
		def decryptedBuffer = ByteBuffer.allocateNative(numBytesForIndex)
		input.readFully(encryptedBuffer.array())
		blowfish.decrypt(encryptedBuffer.array(), 0, decryptedBuffer.array(), 0, decryptedBuffer.capacity())

		def decryptedIndexBuffer = ByteBuffer.allocateNative(numBytesForIndex + 2)
			.put(headerDecrypted)
			.put(decryptedBuffer)
			.rewind()
		entries = new MixFileEntry[numEntries]
		numEntries.times { index ->
			entries[index] = new MixFileEntry(decryptedIndexBuffer)
		}

		baseEntryOffset = SIZE_FLAG + SIZE_BLOWFISH_SOURCE_KEY + SIZE_ENCRYPTED_BLOCK + numBytesForIndex
	}
}
