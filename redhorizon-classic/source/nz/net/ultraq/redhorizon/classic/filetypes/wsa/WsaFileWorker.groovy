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

package nz.net.ultraq.redhorizon.classic.filetypes.wsa

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.codecs.XORDelta
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import java.nio.ByteBuffer

/**
 * A worker for decoding WSA file frame data.
 * 
 * @author Emanuel Rabina
 */
@PackageScope
@TupleConstructor(defaults = false)
class WsaFileWorker extends Worker {

	private static final Logger logger = LoggerFactory.getLogger(WsaFileWorker)

	@Delegate
	final WsaFile wsaFile
	final NativeDataInputStream input

	@Override
	void work() {

		Thread.currentThread().name = 'WsaFile :: Decoding'
		logger.debug('WsaFile decoding started')

		def frameSize = width * height
		def xorDelta = new XORDelta(frameSize)
		def lcw = new LCW()

		// Decode frame by frame
		for (def frame = 0; canContinue && frame < numFrames; frame++) {
			def colouredFrame = average('WsaFile - Decoding frame', 1f) { ->
				def compressedFrameSize = frameOffsets[frame + 1] - frameOffsets[frame]
				def compressedFrame = ByteBuffer.wrapNative(input.readNBytes(compressedFrameSize))

				def intermediateFrame = ByteBuffer.allocateNative(delta)
				def indexedFrame = ByteBuffer.allocateNative(frameSize)

				lcw.decode(compressedFrame, intermediateFrame)
				xorDelta.decode(intermediateFrame, indexedFrame)

				return indexedFrame.applyPalette(palette)
			}
			trigger(new StreamingFrameEvent(colouredFrame))
		}

		if (!stopped) {
			logger.debug('WsaFile decoding complete')
		}
	}
}
