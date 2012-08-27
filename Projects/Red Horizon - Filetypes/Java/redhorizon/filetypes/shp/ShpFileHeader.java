
package redhorizon.filetypes.shp;

import java.nio.ByteBuffer;

/**
 * Abstract SHP file header class containing only the parts similar between both
 * C&C SHP file and Dune 2 SHP file headers.
 * 
 * @author Emanuel Rabina
 */
public abstract class ShpFileHeader {

	final short numimages;

	/**
	 * Constructor, fills-out the common number-of-images part.
	 * 
	 * @param numimages The number of frames the SHP file contains.
	 */
	ShpFileHeader(short numimages) {

		this.numimages = numimages;
	}

	/**
	 * Returns this header in it's <tt>ByteBuffer</tt> representation.
	 * 
	 * @return <tt>ByteBuffer</tt> containing this header's data.
	 */
	abstract ByteBuffer toByteBuffer();
}
