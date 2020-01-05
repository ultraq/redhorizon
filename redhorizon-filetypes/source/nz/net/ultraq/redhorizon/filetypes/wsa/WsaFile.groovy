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

import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VgaPalette
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 * Implementation of the WSA file format as used in Tiberium Dawn and Red Alert.
 * A WSA file is a full-screen animation (Westwood Studios Animation?) that is
 * typically looped or played to the last frame, with some other sounds taking
 * up background music duties.
 * <p>
 * For more information about the C&C WSA file, see: http://vladan.bato.net/cnc/ccfiles4.txt
 * For all the weird nuances surrounding the WSA file and the versions it has
 * gone through, see: http://www.shikadi.net/moddingwiki/Westwood_WSA_Format
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('wsa')
class WsaFile implements AnimationFile, Streaming {

	private static final short FLAG_HAS_PALETTE = 0x01

	private final NativeDataInputStream input

	// File header
	final int numFrames // Stored in file as short
	final short x
	final short y
	final int width     // Stored in file as short
	final int height    // stored in file as short
	final short delta
	final short flags
	final int[] frameOffsets
	final Palette palette

	final ColourFormat format = ColourFormat.FORMAT_RGB
	final float frameRate = 10f
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
		delta     = input.readShort() + 37 // https://github.com/ultraq/redhorizon/issues/4
		flags     = input.readShort()

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

		def frames = []
		Executors.newSingleThreadExecutor().executeAndShutdown { executorService ->
			executorService
				.submit(streamingDataWorker.addDataHandler { type, data ->
					frames << data
				})
				.get()
		}
		return frames
	}

	/**
	 * Return a worker that can be used for streaming the animation's frames.  The
	 * data will be passed to the configured handlers under the {@code frame} key.
	 * 
	 * @return Worker for streaming animation data.
	 */
	@Override
	Worker getStreamingDataWorker() {

		return new WsaFileWorker(this, input)
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
			"Contains ${numFrames} frames to run at ${String.format('%.2f', frameRate)}fps"
		].join(', ')
	}
}
