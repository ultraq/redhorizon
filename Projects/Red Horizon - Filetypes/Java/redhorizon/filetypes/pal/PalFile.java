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

package redhorizon.filetypes.pal;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.PaletteFile;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Implementation of the Red Alert PAL file.  Essentially just an array of the
 * 256 colours (in VGA 6-bits-per-channel format).
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("pal")
public class PalFile extends AbstractFile implements PaletteFile {

	private static final int PALETTE_SIZE = 256;

	private ByteBuffer palettedata;

	/**
	 * Constructor, creates a new palette file using the given name and data.
	 * 
	 * @param name		  The name of the palette.
	 * @param bytechannel The data of the palette.
	 */
	public PalFile(String name, ReadableByteChannel bytechannel) {

		super(name);

		// Fills the palette data
		palettedata = ByteBuffer.allocate(768);
		bytechannel.read(palettedata);
		palettedata.rewind();

		// NOTE: VGA palettes used only 6 bits per byte, meaning they had to be multiplied
		//       by 4 to reach the colour value they represent
		for (int i = 0; i < 768; i++) {
			palettedata.put(i, (byte)(palettedata.get(i) << 2));
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

		return FORMAT_RGB;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getPaletteData() {

		return new ReadableByteChannelAdapter(palettedata);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {

		return PALETTE_SIZE;
	}
}
