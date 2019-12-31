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

package nz.net.ultraq.redhorizon.filetypes.wsa

import nz.net.ultraq.redhorizon.codecs.XORDelta
import nz.net.ultraq.redhorizon.codecs.LCW
import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import java.nio.ByteBuffer

/**
 * Implementation of the Dune 2 WSA file.  It shares many similarities with the
 * format in the Command & Conquer games, but missing positional information for
 * placing the animation and an internal palette.
 * <p>
 * The Dune 2 WSA file is only used for the conversion utility, and does not
 * participate in the Red Horizon game.
 * <p>
 * File header:
 * <ul>
 *   <li>NumFrames (2 bytes) - the number of frames of animation in the file</li>
 *   <li>FrameWidth (2 bytes) - width of each frame</li>
 *   <li>FrameHeight (2 bytes) - height of each frame</li>
 *   <li>Delta (2 bytes) - buffer size required to unpack frame data</li>
 *   <li>Flags (2 bytes) - unused in the Dune 2 version?</li>
 *   <li>Offsets[NumImages + 1] (4 bytes each) - offset to the image data for
 *     each frame.  The last offset points to the end of the file.</li>
 * </ul>
 * After that begins the image data.  Each image needs to be decompressed using
 * LCW, then XORDelta.
 * 
 * @author Emanuel Rabina
 */
class WsaFileDune2 {

	// File header
	final short numFrames
	final short width
	final short height
	final short delta
	final short flags
	final int[] frameOffsets
	final ByteBuffer[] frames

	/**
	 * Constructor, creates a new Dune 2 WSA file from the data in the input
	 * stream.
	 * 
	 * @param inputStream
	 */
	WsaFileDune2(InputStream inputStream) {

		def input = new NativeDataInputStream(inputStream)

		// File header
		numFrames = input.readShort()
		width     = input.readShort()
		height    = input.readShort()
		delta     = input.readShort() + 33 // https://github.com/ultraq/redhorizon/issues/4
		flags     = input.readShort()

		// Frame offsets
		frameOffsets = new int[numFrames + 1]
		frameOffsets.length.times { i ->
			frameOffsets[i] = input.readInt()
		}

		// Frame data
		def frameSize = width * height
		def xorDelta = new XORDelta(frameSize)
		def lcw = new LCW()

		frames = new ByteBuffer[numFrames]
		frames.length.times { frame ->
			def compressedFrameSize = frameOffsets[frame + 1] - frameOffsets[frame]
			def compressedFrame = ByteBuffer.wrapNative(input.readNBytes(compressedFrameSize))

			def intermediateFrame = ByteBuffer.allocateNative(delta)
			def indexedFrame = ByteBuffer.allocateNative(frameSize)

			lcw.decode(compressedFrame, intermediateFrame)
			xorDelta.decode(intermediateFrame, indexedFrame)

			frames[frame] = indexedFrame
		}
	}

	/**
	 * Returns some information on this WSA file.
	 *
	 * @return WSA file info.
	 */
	@Override
	String toString() {

		return """
			WSA file (Dune 2), ${width}x${height}, 8-bit (requires external palette)
			Contains ${numFrames} frames to run at ${String.format('%.2f', delta / 1024)}fps
		""".stripIndent().trim()
	}
}
