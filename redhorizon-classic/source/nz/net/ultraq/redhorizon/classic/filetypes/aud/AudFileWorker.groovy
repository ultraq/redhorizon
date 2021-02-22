/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.filetypes.aud

import nz.net.ultraq.redhorizon.classic.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.classic.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import java.nio.ByteBuffer

/**
 * A worker for decoding AUD file sound data.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
@TupleConstructor(defaults = false)
class AudFileWorker extends Worker {

	private static final Logger logger = LoggerFactory.getLogger(AudFileWorker)

	@Delegate
	final AudFile audFile
	final NativeDataInputStream input

	@Override
	void work() {

		Thread.currentThread().name = "AudFile :: Decoding"
		logger.debug('AudFile decoding started')

		def decoder = type == AudFile.TYPE_IMA_ADPCM ? new IMAADPCM16bit() : new WSADPCM8bit()

		// Decompress the aud file data by chunks
		def headerSize = input.bytesRead
		while (canContinue && input.bytesRead < headerSize + compressedSize) {

			// Chunk header
			def compressedSize = input.readShort()
			def uncompressedSize = input.readShort()
			assert input.readInt() == 0x0000deaf : 'AUD chunk header ID should be "0x0000deaf"'

			// Decode
			def sample = decoder.decode(
				ByteBuffer.wrapNative(input.readNBytes(compressedSize)),
				ByteBuffer.allocateNative(uncompressedSize)
			)

			trigger(new StreamingSampleEvent(sample))
		}

		if (!stopped) {
			logger.debug('AudFile decoding complete')
		}
	}
}
