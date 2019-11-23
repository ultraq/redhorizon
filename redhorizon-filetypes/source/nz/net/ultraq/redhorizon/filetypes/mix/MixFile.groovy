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

import blowfishj.BlowfishECB
import nz.net.ultraq.redhorizon.filetypes.ArchiveFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.nio.channels.SeekableByteChannelView

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel

/**
 * Implementation of a Red Alert MIX file.  The MIX format is a file package,
 * much like a ZIP file.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('mix')
class MixFile implements ArchiveFile<MixRecord> {

	private static final int FLAG_CHECKSUM  = 0x00010000
	private static final int FLAG_ENCRYPTED = 0x00020000
	private static final int FLAG_SIZE      = 4

	private static final int KEY_SIZE_BLOWFISH = 56
	private static final int KEY_SIZE_SOURCE   = 80

	private static final int   ENCRYPT_BLOCK_SIZEI = 8
	private static final float ENCRYPT_BLOCK_SIZEF = 8f

	private final FileChannel fileChannel
	private final boolean checksum
	private final boolean encrypted
	private final MixFileHeader mixHeader
	private final MixRecord[] mixRecords

	/**
	 * The number of files inside the MIX file.
	 */
	@Lazy
	private int numFiles = {
		return mixHeader.numFiles & 0xffff
	}()

	/**
	 * The number of bytes to adjust the offset values in a record, by calculating
	 * the size of all the data that comes before the first internal file in the
	 * MIX file.
	 */
	@Lazy
	private int offsetAdjustSize = {
		def flag = checksum || encrypted ? FLAG_SIZE : 0
		def encryption = encrypted ? KEY_SIZE_SOURCE : 0
		def header = encrypted ? MixFileHeader.HEADER_SIZE + 2 : MixFileHeader.HEADER_SIZE
		def index = MixRecord.RECORD_SIZE * numFiles
		if (encrypted) {
			index = (int)Math.ceil(index / ENCRYPT_BLOCK_SIZEF) * ENCRYPT_BLOCK_SIZEI
		}
		return flag + header + encryption + index
	}()

	/**
	 * Constructor, creates a mix file from a proper file on the file system.
	 * 
	 * @param fileChannel The mix file proper.
	 */
	MixFile(FileChannel fileChannel) {

		this.fileChannel = fileChannel

		// Find out if this file has a checksum/encryption
		def flagBuffer = ByteBuffer.allocate(FLAG_SIZE)
		fileChannel.readAndRewind(flagBuffer)
		def flag = flagBuffer.getInt()
		checksum  = (flag & FLAG_CHECKSUM)  != 0
		encrypted = (flag & FLAG_ENCRYPTED) != 0
		ByteBuffer recordsBuffer

		// If encrypted, decrypt the mixheader and index
		if (encrypted) {

			// Perform the public -> private/Blowfish key function
			def keySource = ByteBuffer.allocate(KEY_SIZE_SOURCE)
			fileChannel.read(keySource)
			def key = ByteBuffer.allocate(KEY_SIZE_BLOWFISH)
			MixFileKey.getBlowfishKey(keySource, key)
			def blowfish = new BlowfishECB()
			blowfish.initialize(key.array(), 0, key.capacity())

			// Decrypt the mixheader
			def headerEncrypted = ByteBuffer.allocate(ENCRYPT_BLOCK_SIZEI)
			def headerDecrypted = ByteBuffer.allocate(ENCRYPT_BLOCK_SIZEI)
			fileChannel.read(headerEncrypted)
			blowfish.decrypt(headerEncrypted.array(), 0, headerDecrypted.array(), 0, headerDecrypted.capacity())
			mixHeader = new MixFileHeader(headerDecrypted)

			// Now figure-out how many more on 8-byte blocks (+2) to decrypt
			def numBlocks = (int)Math.ceil((MixRecord.RECORD_SIZE * numFiles) / ENCRYPT_BLOCK_SIZEF)
			def numBytes = numBlocks * 8

			def encryptedBuffer = ByteBuffer.allocate(numBytes)
			def decryptedBuffer = ByteBuffer.allocate(numBytes)
			fileChannel.read(encryptedBuffer)
			blowfish.decrypt(encryptedBuffer.array(), 0, decryptedBuffer.array(), 0, decryptedBuffer.capacity())

			recordsBuffer = ByteBuffer.allocate(numBytes + 2)
			recordsBuffer.put(headerDecrypted).put(decryptedBuffer).rewind()
		}

		// If not encrypted, just read the straight data
		else {

			// Read the mixheader
			fileChannel.position(0)
			def headerBuffer = ByteBuffer.allocate(MixFileHeader.HEADER_SIZE)
			fileChannel.readAndRewind(headerBuffer)
			mixHeader = new MixFileHeader(headerBuffer)

			// Now figure-out how much more of the file is the index
			def numBlocks = MixRecord.RECORD_SIZE * numFiles
			def numBytes = numBlocks * 8

			recordsBuffer = ByteBuffer.allocate(numBytes)
			fileChannel.readAndRewind(recordsBuffer)
		}

		// Take all the data and turn it into the index records
		mixRecords = new MixRecord[numFiles]
		for (int i = 0; i < mixRecords.length; i++) {
			mixRecords[i] = new MixRecord(recordsBuffer)
		}
	}

	/**
	 * Calculates an ID for a {@link MixRecord} given the original file
	 * name for the entry to which it is referring to.
	 * 
	 * @param filename The original filename of the item in the MIX body.
	 * @return The ID of the entry from the filename.
	 */
	private static int calculateId(String filename) {

		def name = filename.toUpperCase()
		def id = 0

		for (def i = 0; i < name.length(); ) {
			def a = 0
			for (def j = 0; j < 4; j++) {
				a >>>= 8
				if (i < name.length()) {
					a += name.charAt(i) << 24
				}
				i++
			}
			id = (id << 1 | id >>> 31) + a
		}
		return id
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ReadableByteChannel getEntryData(MixRecord record) {

		return new SeekableByteChannelView(fileChannel, record.offset + offsetAdjustSize, record.size)
	}

	/**
	 * Returns a record for an item in the MIX file, instead of the item itself.
	 * Uses a binary search algorithm to locate the record.
	 * 
	 * @param name Name of the item and the record.
	 * @return <tt>MixRecord</tt> object of the record for the item.
	 */
	@Override
	MixRecord getEntry(String name) {

		def itemId = calculateId(name)

		// Binary search for the record with the calculated value
		MixRecord record = null
		def lo = 0
		def hi = mixRecords.length - 1

		while (lo <= hi) {
			def mid = (lo + hi) >> 1
			def midval = mixRecords[mid].id

			if (itemId < midval) {
				hi = mid - 1
			}
			else if (itemId > midval) {
				lo = mid + 1
			}
			else {
				record = mixRecords[mid]
				break
			}
		}

		// Set the name on the record
		if (record != null) {
			record.name = name
		}
		return record
	}
}
