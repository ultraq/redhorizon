
// ========================================
// Scanner's Java - Palette alpha interface
// ========================================

package redhorizon.filetypes;

import java.util.Map;

/**
 * Interface outlining methods to apply alpha values to an existing palette.
 * The array in the map returned by {@link #getAlphaMap()} contains RGBA values
 * that will override whatever values are at a the map's key's index.
 * 
 * @author Emanuel Rabina
 */
public interface PaletteAlpha {

	/**
	 * Retrieves a map with palette indices (0-255) as the key to their intended
	 * RGBA values.  If an RGBA value should replace a colour channel, the value
	 * of the mapped array will be between 0-255.  Otherwise, it is -1.<br>
	 * eg:<br>
	 *  Key: 10 -> Value: { -1, -1, -1, 127 }<br>
	 *  The values apply to palette index 10, but only replaces the A channel.
	 *  RGB should be the same as whatever palette index 10 is at the moment.
	 * 
	 * @return A mapping of indices to new RGBA values.
	 */
	public Map<Integer,int[]> getAlphaMap();

	/**
	 * Returns a name with which to use to identify this alpha map.
	 * 
	 * @return A name for the alpha map.
	 */
	public String getAlphaName();
}
