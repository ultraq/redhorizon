/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.filetypes.wsa;

import redhorizon.filetypes.AnimationFile;
import redhorizon.filetypes.FileType;
import redhorizon.filetypes.Paletted;
import redhorizon.filetypes.WritableFile;
import redhorizon.filetypes.png.PngFile;
import redhorizon.utilities.CodecUtility;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;

/**
 * Implementation of the Dune 2 WSA file, used for extracting information and
 * animation data from the file.  This type supports the ability to save itself
 * to a file.
 * <p>
 * Details on this filetype are not very common, so I've written comments
 * (visible in the source, not in this JavaDoc) on how the file is structured,
 * so that others it can help others understand how it works if ever they wish
 * to write programs to utilize the file.
 * <p>
 * The Dune 2 WSA file is only used for the conversion utility, and does not
 * participate in the Red Horizon game.
 * 
 * @author Emanuel Rabina
 */

/* 
 * ======================
 * Dune 2 WSA file format
 * ======================
 * 
 * The Dune 2 WSA file, is a standard animation filetype.  The file is
 * structured as follows:
 * 
 * File header:
 *   NumFrames (2 bytes)    - the number of frames of animation in the file
 *   FrameWidth (2 bytes)   - width of each frame
 *   FrameHeight (2 bytes)  - height of each frame
 *   Delta (4 bytes)        - frames/sec = delta/1024
 *   Offsets[NumImages + 1] - offset to the image data for each frame.  The last
 *     (4 bytes each)         offset points to the end of the file.
 * 
 * After that begins the image data.  Each image needs to be decompressed using
 * Format80, then Format40 (XOR'ed over the previous image data, or a blank
 * frame for the first image).
 */
@FileType(AnimationFile.class)
public class WsaFileDune2 extends WsaFile<WsaFileHeaderDune2> implements Paletted, WritableFile {

	/**
	 * Constructor, creates a new Dune 2 wsa file with the given name and file
	 * data.
	 * 
	 * @param name		  The name of this file.
	 * @param filechannel Channel into the file.
	 */
	public WsaFileDune2(String name, FileChannel filechannel) {

		super(name);

		try {
			// Read file header
			ByteBuffer headerbytes = ByteBuffer.allocate(WsaFileHeaderDune2.HEADER_SIZE);
			filechannel.read(headerbytes);
			headerbytes.rewind();
			wsaheader = new WsaFileHeaderDune2(headerbytes);

			// Read offsets
			wsaoffsets = new int[numImages() + 2];
			ByteBuffer offsets = ByteBuffer.allocate(wsaoffsets.length * 4);
			filechannel.read(offsets);
			offsets.rewind();
			for (int i = 0; i < wsaoffsets.length; i++) {
				wsaoffsets[i] = offsets.getInt();
			}

			// Read image data
			wsaframes = new ByteBuffer[numImages()];
			ByteBuffer lastbytes = ByteBuffer.allocate(width() * height());
			for (int i = 0; i < wsaframes.length; i++) {
				int offset = wsaoffsets[i];
				int sourcelength = wsaoffsets[i + 1] - offset;

				// Source frame data
				ByteBuffer sourcebytes = ByteBuffer.allocate(sourcelength);
				filechannel.read(sourcebytes, offset);
				sourcebytes.rewind();

				// Intermediate and final frame data
				int framesize = width() * height();
				ByteBuffer intbytes   = ByteBuffer.allocate(framesize);
				ByteBuffer framebytes = ByteBuffer.allocate(framesize);

				// First decompress from Format80, then decode as Format40
				CodecUtility.decodeFormat80(sourcebytes, intbytes);
				CodecUtility.decodeFormat40(intbytes, framebytes, lastbytes);

				wsaframes[i] = framebytes;
			}
		}
		finally {
			filechannel.close();
		}
	}

	/**
	 * Constructor, builds this filetype using the data from a
	 * <code>PngFile</code> type.
	 * 
	 * @param name	  The name of this file.
	 * @param pngfile Fully read PNG file to extract data from.
	 * @param params  Additional parameters: width, height, numimgs, framerate,
	 * 				  looping.
	 */
	public WsaFileDune2(String name, PngFile pngfile, String... params) {

		super(name, pngfile, params);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float adjustmentFactor() {

		return 1f;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void buildFile(int width, int height, float framerate, boolean looping, ByteBuffer[] frames) {

		// Build file header
		wsaheader = new WsaFileHeaderDune2(looping ? (short)(frames.length - 1) : (short)frames.length,
				(short)width, (short)height, (int)(framerate * 1024f));

		// Build frames
		wsaframes = frames;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void close() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float frameRate() {

		return wsaheader.delta / 1024f;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImagesData() {

		return new ReadableByteChannelAdapter(wsaframes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return wsaheader.height & 0xff;
	}

	/**
	 * Returns some information on this Dune 2 WSA file.
	 * 
	 * @return Dune 2 WSA file info.
	 */
	@Override
	public String toString() {

		DecimalFormat dp2 = new DecimalFormat("0.00");

		return filename + " (Dune 2 WSA file)" +
			"\n  Number of images: " + numImages() + " " + (isLooping() ? " + 1 loop frame" : "") + 
			"\n  Framerate: " + dp2.format(frameRate()) + "fps" +
			"\n  Image width: " + width() +
			"\n  Image height: " + height() +
			"\n  Colour depth: 8-bit";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return wsaheader.width & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(GatheringByteChannel outputchannel) {

		int numimages = numImages();

		// Build header
		ByteBuffer header = wsaheader.toByteBuffer();

		// Encode each frame, construct matching offsets
		ByteBuffer[] frames = new ByteBuffer[isLooping() ? numimages + 1 : numimages];
		ByteBuffer lastbytes = ByteBuffer.allocate(width() * height());

		int offsetsize = (numimages + 2) * 4;
		ByteBuffer frameoffsets = ByteBuffer.allocate(offsetsize);
		int offsettotal = WsaFileHeaderDune2.HEADER_SIZE + offsetsize;

		for (int i = 0; i < frames.length; i++) {
			ByteBuffer framebytes = wsaframes[i];
			ByteBuffer frameint = ByteBuffer.allocate((int)(framebytes.capacity() * 1.5));
			ByteBuffer frame    = ByteBuffer.allocate((int)(framebytes.capacity() * 1.5));

			// First encode in Format40, then Format80
			CodecUtility.encodeFormat40(framebytes, frameint, lastbytes);
			CodecUtility.encodeFormat80(frameint, frame);

			frames[i] = frame;
			lastbytes = framebytes;

			frameoffsets.putInt(offsettotal);
			offsettotal += frame.limit();
		}

		// Last offset for EOF
		frameoffsets.putInt(offsettotal);
		frameoffsets.rewind();


		// Write file to disk
		outputchannel.write(header);
		outputchannel.write(frameoffsets);
		outputchannel.write(frames);
	}
}
