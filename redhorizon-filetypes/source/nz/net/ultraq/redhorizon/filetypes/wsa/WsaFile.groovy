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

import nz.net.ultraq.redhorizon.codecs.Format40
import nz.net.ultraq.redhorizon.codecs.Format80
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VgaPalette
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.utilities.ImageUtility

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService

/**
 * Implementation of the WSA file format as used in Tiberium Dawn and Red Alert.
 * A WSA file is a full-screen animation (Westwood Studios Animation?) that is
 * typically looped or played to the last frame, with some other sounds taking
 * up background music duties.
 * <p>
 * For more information about the C&C WSA file, see: http://vladan.bato.net/cnc/ccfiles4.txt
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('wsa')
class WsaFile implements AnimationFile, Streaming {

	private static final Logger logger = LoggerFactory.getLogger(WsaFile)

	private final NativeDataInputStream input

	// File header
	final int numFrames // Stored in file as short
	final short x
	final short y
	final int width     // Stored in file as short
	final int height    // stored in file as short
	final int delta
	final int[] frameOffsets
	final Palette palette

	final ColourFormat format = ColourFormat.FORMAT_RGB
	final float frameRate
	final boolean looping

	/**
	 * Constructor, creates a new WSA file from the data in the input stream.
	 * 
	 * @param input
	 */
	WsaFile(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		numFrames = input.readShort()
		x         = input.readShort()
		y         = input.readShort()
		width     = input.readShort()
		height    = input.readShort()
		delta     = input.readInt()

		frameRate = 1 / (delta / 1024) * 1000

		// Frame offsets
		frameOffsets = new int[numFrames + 2]
		frameOffsets.length.times { i ->
			frameOffsets[i] = input.readInt()
		}

		looping = frameOffsets[frameOffsets.length - 1] != 0

		// Internal VGA palette
		palette = new VgaPalette(256, format, input)
	}

	@Override
	ByteBuffer[] getFrameData(ExecutorService executorService) {

		def frames = []
		executorService
			.submit(getStreamingDataWorker { frames << it })
			.get()
		return frames
	}

	/**
	 * Return a worker that can be used for streaming the animation's frames to
	 * the {@code frameHandler} closure.
	 * 
	 * @param frameHandler
	 * @return Worker for streaming animation data.
	 */
	@Override
	Worker getStreamingDataWorker(Closure frameHandler) {

		return new Worker() {
			@Override
			void work() {

				Thread.currentThread().name = 'WsaFile :: Decoding'
				logger.debug('WsaFile decoding started')

				def frameSize = width * height
				def format40Decoder = new Format40()
				def format80Decoder = new Format80()
				def lastIndexedFrame = ByteBuffer.allocateNative(frameSize)

				// Decode frame by frame
				for (def frame = 0; canContinue && frame < numFrames; frame++) {
					def compressedFrameSize = frameOffsets[frame + 1] - frameOffsets[frame]
					def compressedFrame = ByteBuffer.wrapNative(input.readNBytes(compressedFrameSize))

					def intermediateFrame = ByteBuffer.allocateNative(frameSize)
					def indexedFrame = ByteBuffer.allocateNative(frameSize)

					format80Decoder.decode(compressedFrame, intermediateFrame)
					format40Decoder.decode(intermediateFrame, indexedFrame, lastIndexedFrame)

					def colouredFrame = ImageUtility.applyPalette(indexedFrame, palette)

					frameHandler(colouredFrame)
					lastIndexedFrame = indexedFrame
				}

				if (!stopped) {
					logger.debug('WsaFile decoding complete')
				}
			}
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
			WSA file (C&C), ${width}x${height}, 18-bit with internal palette of 256 colours
			Contains ${numFrames} frames to run at ${String.format('%.2f', frameRate)}fps
		""".stripIndent().trim()
	}
}
