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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.codecs.XORDelta
import nz.net.ultraq.redhorizon.classic.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.graphics.ImageDecoder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

/**
 * An image decoder for the WSA file format as used in Tiberium Dawn and Red
 * Alert.  A WSA file is a full-screen animation (Westwood Studios Animation?)
 * that is typically looped or played to the last frame, with some other sounds
 * taking up background music duties.
 * <p>
 * For more information about the C&C WSA file, see:
 * <a href="http://vladan.bato.net/cnc/ccfiles4.txt" target="_top">http://vladan.bato.net/cnc/ccfiles4.txt</a>.
 * For all the weird nuances surrounding the WSA file and the versions it has
 * gone through, see:
 * <a href="http://www.shikadi.net/moddingwiki/Westwood_WSA_Format" target="_top">http://www.shikadi.net/moddingwiki/Westwood_WSA_Format</a>
 *
 * @author Emanuel Rabina
 */
class WsaFileDecoder implements ImageDecoder, FileTypeTest {

	private static final Logger logger = LoggerFactory.getLogger(WsaFileDecoder)

	static final short FLAG_HAS_PALETTE = 0x01

	final String[] supportedFileExtensions = ['wsa']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		// File header
		var numFrames = input.readUnsignedShort()
		assert numFrames > 0

		var x = input.readShort()
		var y = input.readShort()

		var width = input.readUnsignedShort()
		assert width > 0

		var height = input.readUnsignedShort()
		assert height > 0

		var delta = input.readUnsignedShort() + 37 // https://github.com/ultraq/redhorizon/issues/4

		var flags = input.readUnsignedShort()
		assert (flags & 0x0001) == flags

		// Frame offsets
		var frameOffsets = new int[numFrames + 2]
		frameOffsets.length.times { i ->
			frameOffsets[i] = input.readInt()
		}

		var looping = frameOffsets[frameOffsets.length - 1] != 0

		// Internal VGA palette
		var palette = null
		if (flags & FLAG_HAS_PALETTE) {
			palette = new VgaPalette(256, 3, input)
		}

		trigger(new HeaderDecodedEvent(width, height, 3, numFrames, 10f))

		var frameSize = width * height
		var xorDelta = new XORDelta(frameSize)
		var lcw = new LCW()
		var framesDecoded = 0

		// Decode frame by frame
		try {
			for (var frame = 0; frame < numFrames && !Thread.currentThread().interrupted; frame++) {
				var colouredFrame = average('Decoding frame', 1f, logger) { ->
					var deltaFrame = lcw.decode(
						ByteBuffer.wrapNative(input.readNBytes(frameOffsets[frame + 1] - frameOffsets[frame])),
						ByteBuffer.allocateNative(delta)
					)
					var indexedFrame = xorDelta.decode(deltaFrame, ByteBuffer.allocateNative(frameSize))
					return indexedFrame.applyPalette(palette)
				}
				trigger(new FrameDecodedEvent(width, height, palette.format, colouredFrame))
				framesDecoded++
				Thread.yield()
			}
		}
		catch (InterruptedException ignored) {
			logger.debug('Decoding was interrupted')
		}

		return new DecodeSummary(width, height, 3, framesDecoded,
			"WSA file (C&C), ${width}x${height}, ${palette ? '18-bit w/ 256 colour palette' : '(no palette)'}, ${numFrames} frames")
	}

	@Override
	void test(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		// File header
		var numFrames = input.readUnsignedShort()
		assert numFrames > 0

		input.skipBytes(4)

		var width = input.readUnsignedShort()
		assert width > 0

		var height = input.readUnsignedShort()
		assert height > 0

		input.skipBytes(2)

		var flags = input.readUnsignedShort()
		assert (flags & 0x0001) == flags
	}
}
