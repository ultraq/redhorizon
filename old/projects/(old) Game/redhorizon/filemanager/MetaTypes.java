
// =====================================================
// Scanner's Java - Different file formats per file type
// =====================================================

package redhorizon.filemanager;

/**
 * Description of the types of data found within the file types used for Red
 * Horizon.  Stored as bit patterns of the various file types that exist.  A
 * file with a given extension can be several meta types (eg: the INI file can
 * be a settings, map, or mission file).
 * 
 * @author Emanuel Rabina
 */
public interface MetaTypes {

	// Primitive file types
	public static final int NULL   = 0;
	public static final int BINARY = 1 << 0;
	public static final int TEXT   = 1 << 20;

	// Basic binary file types
	public static final int ANIMATION  = BINARY | (1 << 1);
	public static final int ARCHIVE    = BINARY | (1 << 2);
	public static final int IMAGE      = BINARY | (1 << 3);
	public static final int IMAGES     = BINARY | (1 << 4);
	public static final int PALETTE    = BINARY | (1 << 5);
	public static final int SOUND      = BINARY | (1 << 6);
	public static final int SOUNDTRACK = BINARY | (1 << 7);
	public static final int VIDEO      = BINARY | (1 << 8);

	// Basic text file types
	public static final int MAP     = TEXT | (1 << 1);
	public static final int MISSION = TEXT | (1 << 2);
}
