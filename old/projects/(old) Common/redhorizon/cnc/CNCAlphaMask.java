
// ========================================
// Scanner's Java - C&C transparency values
// ========================================

package redhorizon.cnc;

import redhorizon.filetypes.PaletteAlpha;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerated type containing the various alpha adjustments made to the C&C
 * palette.  At the moment, there is only 1 alpha mask used across both Red
 * Alert and Tiberium Dawn.
 * 
 * @author Emanuel Rabina
 */
public enum CNCAlphaMask implements PaletteAlpha {

	// Alpha values for index 0 and 4 (transparent & shadow in C&C)
	FULL        (new int[]{ -1, -1, -1, 0 }, new int[]{ 0, 0, 0, 127 }),
	INDEX0_ONLY (new int[]{ -1, -1, -1, 0 }, null),
	INDEX4_ONLY (null,                       new int[]{ 0, 0, 0, 127 });

	private final Map<Integer,int[]> alphamap = new HashMap<Integer,int[]>();

	/**
	 * Constructor, applies the alpha values to the 0th and 4th indices.
	 * 
	 * @param a0 Alpha 0 RGBA values.
	 * @param a4 Alpha 4 RGBA values.
	 */
	private CNCAlphaMask(int[] a0, int[] a4) {

		if (a0 != null) {
			alphamap.put(0, a0);
		}
		if (a4 != null) {
			alphamap.put(4, a4);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<Integer,int[]> getAlphaMap() {

		return alphamap;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAlphaName() {

		return name();
	}

}
