
package redhorizon.filetypes.mix;

import java.nio.ByteBuffer;

import redhorizon.filetypes.ArchiveFileEntry;

/**
 * Representation of a Red Alert MIX file index record, found in the header of
 * MIX files to indicate where content can be located within the body.
 * 
 * @author Emanuel Rabina
 */
public class MixRecord implements ArchiveFileEntry, Comparable<MixRecord> {

	static final int RECORD_SIZE = 12;

	String name;	// Name cannot be determined initially
	final int id;
	final int offset;
	final int length;

	/**
	 * Constructor, assigns the ID, offset, and length of this entry from the
	 * current byte channel.
	 * 
	 * @param bytes Buffer containing the entry bytes.
	 */
	MixRecord(ByteBuffer bytes) {

		id     = bytes.getInt();
		offset = bytes.getInt();
		length = bytes.getInt();
	}

	/**
	 * Compares this record to the other, returns negative, zero, or positive if
	 * this record's ID is less than, equal to, or greater than the one being
	 * compared to.
	 * 
	 * @param other The other <tt>MixRecord</tt> to compare with.
	 * @return -1, 0, 1 :: less-than, equal to, greater than.
	 */
	@Override
	public int compareTo(MixRecord other) {

		int thisid = this.id;
		int otherid = other.id;

		if (thisid < otherid) {
			return -1;
		}
		else if (thisid > otherid) {
			return 1;
		}
		else {
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSize() {

		return length;
	}
}
