
package redhorizon.filetypes.vqa;

import java.nio.ByteBuffer;

/**
 * Representation of the VQA file's lookup table.  In the first chunk, there is
 * 1 complete lookup table, but in subsequent ones only fractions of the table
 * are available.  A new one is created once all these fractions add to a new
 * table, which should then replace the previous one.
 * 
 * @author Emanuel Rabina
 */
public class VqaTable {

	final ByteBuffer table;

	/**
	 * Constructor, builds an initial lookup table of the given dimensions.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing a complete lookup table.
	 */
	VqaTable(ByteBuffer bytes) {

		table = bytes;
	}

	/**
	 * Returns the value at the given position in the lookup table.
	 * 
	 * @param index Position in the lookup table.
	 * @return The value at the given position.
	 */
	byte getValueAt(int index) {

		return table.get(index);
	}
}
