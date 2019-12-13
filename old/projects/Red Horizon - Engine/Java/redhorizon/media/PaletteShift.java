
// ========================================
// Scanner's Java - Palette shift interface
// ========================================

package redhorizon.filetypes;

import redhorizon.filetypes.pal.PalFile;

import java.util.Map;

/**
 * Interface outlining methods required to pass as a shift index on the
 * {@link PalFile}.
 * 
 * @author Emanuel Rabina
 */
public interface PaletteShift {

	/**
	 * Returns a map with palette indices as the key to the intended shift
	 * value.
	 * 
	 * @return A mapping of indices to existing palette indices.
	 */
	public Map<Integer,Integer> getShiftMap();

	/**
	 * Returns some sort of name to identify this palette shift.
	 * 
	 * @return Name for this palette shift.
	 */
	public String getShiftName();
}
