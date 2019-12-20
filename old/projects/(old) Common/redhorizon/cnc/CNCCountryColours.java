
// ====================================
// Scanner's Java - Palette shift index
// ====================================

package redhorizon.cnc;

import redhorizon.filetypes.PaletteShift;
import redhorizon.filetypes.pal.PalFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerated type which contains the available palette shift types.  There are
 * only 8 shift types at current, but not each of them perfectly match-up to the
 * entire colour range exhibited by a specific side.  The only example of this
 * is the Nod side in TD which has red for structures and blue for units.  To
 * cater for this separation (and other potential combos), if applying a shift
 * to a {@link PalFile}, use the one which can be retrieved from the faction
 * information object instead.
 * 
 * @author Emanuel Rabina
 */
public enum CNCCountryColours implements PaletteShift {

	// Shift values
	GOLD   (new int[]{  80,  81,  82,  83,  84,  85,  86,  87,  88,  89,  90,  91 , 92 , 93,  94,  95 }),
	BLUE   (new int[]{ 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175 }),
	RED    (new int[]{ 229, 230, 231, 232, 233, 234, 235,   8, 236, 237, 238, 239, 221, 222, 223, 223 }),
	GREEN  (new int[]{ 208, 208, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 154, 155, 143 }),
	ORANGE (new int[]{ 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223 }),
	BROWN  (new int[]{ 128, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 121, 122, 122, 123, 123 }),
	TEAL   (new int[]{ 224, 224, 225, 225, 226, 184, 185, 186, 187, 188, 188, 189, 190, 190, 191, 191 }),
	MAROON (new int[]{ 200, 200, 201, 202, 203, 203, 204, 205, 206, 206, 207, 221, 222, 222, 223, 223 });

	private final Map<Integer,Integer> shiftmap = new HashMap<Integer,Integer>();

	/**
	 * Constructor, creates the shift indices for the colour type.
	 * 
	 * @param shift Array of values to shift from C&C's 80th index to do
	 * 				faction-specific colouring.
	 */
	private CNCCountryColours(int[] shift) {

		int i = 80;
		for (int shiftval: shift) {
			shiftmap.put(i++, shiftval);
		}
	}

	/**
	 * Returns the matching palette shift of the given name.
	 * 
	 * @param match Name to match with an existing <tt>CNCCountryColours</tt>.
	 * @return Matching palette shift type.
	 */
	public static CNCCountryColours getMatchingType(String match) {

		for (CNCCountryColours palette: CNCCountryColours.values()) {
			if (palette.name().equalsIgnoreCase(match)) {
				return palette;
			}
		}
		throw new EnumConstantNotPresentException(CNCCountryColours.class, match);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<Integer,Integer> getShiftMap() {

		return shiftmap;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getShiftName() {

		return name();
	}
}
