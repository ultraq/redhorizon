
package redhorizon.filetypes.shp;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.FORMAT_INDEXED;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Abstract SHP file class containing only the parts similar between both C&C
 * and Dune 2 SHP files.
 * 
 * @author Emanuel Rabina
 * @param <H> SHP file header implementation type.
 */
public abstract class ShpFile<H extends ShpFileHeader> extends AbstractFile
	implements ImagesFile, Paletted {

	// Read-from information
	H shpfileheader;
	ByteBuffer[] shpimages;

	// Save-to information
	static final String PARAM_WIDTH   = "-w:";
	static final String PARAM_HEIGHT  = "-h:";
	static final String PARAM_NUMIMGS = "-n:";

	static final int MAX_WIDTH   = 65535;
	static final int MAX_NUMIMGS = 65535;

	/**
	 * Constructor, creates a SHP file with the given name.
	 * 
	 * @param name The name of this file.
	 */
	ShpFile(String name) {

		super(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ColourFormat format() {

		return FORMAT_INDEXED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImagesData() {

		return new ReadableByteChannelAdapter(shpimages);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int numImages() {

		return shpfileheader.numimages & 0xffff;
	}
}
