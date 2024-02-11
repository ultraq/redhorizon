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
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingDecoder
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.nio.ByteBuffer
import java.text.DecimalFormat

/**
 * Implementation of the WSA file format as used in Tiberium Dawn and Red Alert.
 * A WSA file is a full-screen animation (Westwood Studios Animation?) that is
 * typically looped or played to the last frame, with some other sounds taking
 * up background music duties.
 * <p>
 * For more information about the C&C WSA file, see:
 * <a href="http://vladan.bato.net/cnc/ccfiles4.txt" target="_top">http://vladan.bato.net/cnc/ccfiles4.txt</a>.
 * For all the weird nuances surrounding the WSA file and the versions it has
 * gone through, see:
 * <a href="http://www.shikadi.net/moddingwiki/Westwood_WSA_Format" target="_top">http://www.shikadi.net/moddingwiki/Westwood_WSA_Format</a>
 *
 * @author Emanuel Rabina
 */
@FileExtensions('wsa')
@SuppressWarnings('GrFinalVariableAccess')
class WsaFile implements AnimationFile, Streaming {

	private static final Logger logger = LoggerFactory.getLogger(WsaFile)

	static final short FLAG_HAS_PALETTE = 0x01

	private final NativeDataInputStream input

	// File header
	final int numFrames // Stored in file as short
	final short x
	final short y
	final int width     // Stored in file as short
	final int height    // stored in file as short
	final int delta
	final short flags
	final int[] frameOffsets
	final Palette palette

	final ColourFormat format = ColourFormat.FORMAT_RGB
	final float frameRate = 10f
	final boolean looping
	final boolean forVgaMonitors = true

	/**
	 * Constructor, creates a new WSA file from the data in the input stream.
	 *
	 * @param input
	 */
	WsaFile(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		numFrames = input.readShort()
		assert numFrames > 0

		x = input.readShort()
		y = input.readShort()

		width = input.readShort()
		assert width > 0

		height = input.readShort()
		assert height > 0

		delta = input.readUnsignedShort() + 37 // https://github.com/ultraq/redhorizon/issues/4
		flags = input.readShort()

		// Frame offsets
		frameOffsets = new int[numFrames + 2]
		frameOffsets.length.times { i ->
			frameOffsets[i] = input.readInt()
		}

		looping = frameOffsets[frameOffsets.length - 1] != 0

		// Internal VGA palette
		if (flags & FLAG_HAS_PALETTE) {
			palette = new VgaPalette(256, format, input)
		}
	}

	@Override
	ByteBuffer[] getFrameData() {

		var decoder = new WsaFileDecoder()
		var frames = []
		decoder.on(StreamingFrameEvent) { event ->
			frames << event.frame
		}
		decoder.run()
		return frames as ByteBuffer[]
	}

	/**
	 * Return a decoder that can be used for streaming the animation's frames.
	 * The decoder will emit {@link StreamingFrameEvent}s for new frames
	 * available.
	 */
	@Override
	StreamingDecoder getStreamingDecoder() {

		return new StreamingDecoder(new WsaFileDecoder(true))
	}

	/**
	 * Returns some information on this WSA file.
	 *
	 * @return WSA file info.
	 */
	@Override
	String toString() {

		return [
			"WSA file (C&C), ${width}x${height}, ${palette ? '18-bit w/ 256 colour palette' : '(no palette)'}",
			"Contains ${numFrames} frames to run at ${new DecimalFormat('0.#').format(frameRate)}fps"
		].join(', ')
	}

	/**
	 * Decode WSA file frame data and emit as {@link StreamingFrameEvent}s.
	 */
	@TupleConstructor
	class WsaFileDecoder implements Runnable, EventTarget {

		final boolean rateLimit

		@Override
		void run() {

			Thread.currentThread().name = 'WsaFile :: Decoding'
			logger.debug('Decoding started')

			def frameSize = width * height
			def xorDelta = new XORDelta(frameSize)
			def lcw = new LCW()

			// Decode frame by frame
			def frame = 0
			while (frame < numFrames && !Thread.interrupted()) {
				def colouredFrame = average('Decoding frame', 1f, logger) { ->
					def indexedFrame = xorDelta.decode(
						lcw.decode(
							ByteBuffer.wrapNative(input.readNBytes(frameOffsets[frame + 1] - frameOffsets[frame])),
							ByteBuffer.allocateNative(delta)
						),
						ByteBuffer.allocateNative(frameSize)
					)
					return indexedFrame.applyPalette(palette)
				}
				trigger(new StreamingFrameEvent(colouredFrame))
				frame++

				if (rateLimit) {
					Thread.sleep(50)
				}
			}

			logger.debug('Decoding complete')
		}
	}
}
