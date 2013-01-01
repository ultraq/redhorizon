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

package redhorizon.filetypes.tmp;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.utilities.BufferUtility;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGBA;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Representation of Red Alert's map tiles.  These are the various bits and
 * pieces which comprise the [MapPack] section of the scenario files.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions({"int", "sno", "tmp"})
public class TmpFileRA extends AbstractFile implements ImagesFile, Paletted {

	private TmpFileHeaderRA tmpheader;
	private ByteBuffer[] tmpimages;

	/**
	 * Constructor, create a new RA template file from existing data.
	 * 
	 * @param name		  The name of this file.
	 * @param bytechannel Data for this file.
	 */
	public TmpFileRA(String name, ReadableByteChannel bytechannel) {

		super(name);

		try {
			// Read header
			ByteBuffer headerbytes = ByteBuffer.allocate(TmpFileHeaderRA.HEADER_SIZE);
			bytechannel.read(headerbytes);
			headerbytes.rewind();
			tmpheader = new TmpFileHeaderRA(headerbytes);

			// Read image offsets
			ByteBuffer offsetsbytes = ByteBuffer.allocate(numImages());
			byte[] imageoffsets = offsetsbytes.array();

			// Read images
			tmpimages = new ByteBuffer[numImages()];
			ByteBuffer imagesdata = BufferUtility.readRemaining(bytechannel);
			for (int i = 0; i < tmpimages.length; i++) {
				byte imageoffset = imageoffsets[i];

				// Skip empty tiles
				if (imageoffset == 0xff) {
					continue;
				}

				// Read data at given offset
				ByteBuffer imagedata = ByteBuffer.allocate(width() * height());
				imagesdata.position(tmpheader.imagedata + (imageoffset * width() * height()));
				imagesdata.get(imagedata.array());
				tmpimages[i] = imagedata;
			}
		}
		finally {
			bytechannel.close();
		}
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
	public ColourFormat format() {

		return FORMAT_RGBA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImagesData() {

		return new ReadableByteChannelAdapter(tmpimages);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return tmpheader.height & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int numImages() {

		return tmpheader.numtiles & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return tmpheader.width & 0xffff;
	}
}
