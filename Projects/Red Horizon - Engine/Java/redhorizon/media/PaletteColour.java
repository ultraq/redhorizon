
// =================================================
// Scanner's Java - Basic RGBA colour representation
// =================================================

package redhorizon.filetypes;

/**
 * Standard RGB(A) colour representation.  Uses 8 bits per channel and can
 * compensate for older VGA palettes which used 6 bits.
 * 
 * @author Emanuel Rabina
 */
public class PaletteColour {

	private final byte red;
	private final byte green;
	private final byte blue;
	private final byte alpha;

	private final boolean hasalpha;

	/**
	 * Constructor, assigns the 3 colour components using the default colour
	 * precision of 8-bits-per-colour.  The alpha component will not be used.
	 * 
	 * @param red	Red component.
	 * @param green Green component.
	 * @param blue	Blue component.
	 */
	public PaletteColour(byte red, byte green, byte blue) {

		this(red, green, blue, false);
	}

	/**
	 * Constructor, assigns the 3 colour components and specifying whether the
	 * input values are 8 or 6 bits per colour.  The alpha component will not be
	 * used.
	 * 
	 * @param red	Red component.
	 * @param green Green component.
	 * @param blue	Blue component.
	 * @param isvga Whether the given values are from a VGA palette and as such
	 * 				need to be adjusted.
	 */
	public PaletteColour(byte red, byte green, byte blue, boolean isvga) {

		this.red   = isvga ? (byte)(red << 2)    : red;
		this.green = isvga ? (byte)(green << 2 ) : green;
		this.blue  = isvga ? (byte)(blue << 2)   : blue;
		this.alpha = 0;

		hasalpha = false;
	}

	/**
	 * Constructor, assigns the 4 colour components using the default colour
	 * precision of 8-bits-per-colour.
	 * 
	 * @param red	Red component.
	 * @param green Green component.
	 * @param blue	Blue component.
	 * @param alpha Alpha component.
	 */
	public PaletteColour(byte red, byte green, byte blue, byte alpha) {

		this.red   = red;
		this.green = green;
		this.blue  = blue;
		this.alpha = alpha;

		hasalpha = true;
	}

	/**
	 * Returns the red component of this colour.
	 * 
	 * @return The R in RGB(A).
	 */
	public byte getRed() {

		return red;
	}

	/**
	 * Returns the green component of this colour.
	 * 
	 * @return The G in RGB(A).
	 */
	public byte getGreen() {

		return green;
	}

	/**
	 * Returns the blue component of this colour.
	 * 
	 * @return The B in RGB(A).
	 */
	public byte getBlue() {

		return blue;
	}

	/**
	 * Returns the alpha component of this colour.
	 * 
	 * @return The A in RGBA.
	 * @throws UnsupportedOperationException If this colour has no alpha
	 * 		   component, ie: this colour wasn't constructed using one of the
	 * 		   RGBA constructors
	 */
	public byte getAlpha() throws UnsupportedOperationException {

		if (!hasalpha) {
			throw new UnsupportedOperationException("Colour does not have an alpha component.");
		}
		return alpha;
	}

	/**
	 * Returns the RGB(A) components as an array.
	 * 
	 * @return <tt>byte[]</tt> with the 0th, 1st, 2nd (and 3rd) bytes
	 * 		   representing the red, green, blue (and alpha) parts respectively.
	 */
	public byte[] toArray() {

		return hasalpha ?
				new byte[]{ getRed(), getGreen(), getBlue(), alpha }:
				new byte[]{ getRed(), getGreen(), getBlue() };
	}

	/**
	 * Returns the RGB(A) components in VGA form as an array
	 * 
	 * @return <tt>byte[]</tt> with the 0th, 1st, 2nd (and 3rd) bytes
	 * 		   representing the red, green, blue (and alpha) parts respectively,
	 * 		   in VGA (6-bit) form.
	 */
	public byte[] toArrayVGA() {

		byte[] array = toArray();
		for (int i = 0; i < array.length; i++) {
			array[i] = (byte)((array[i] & 0xff) >>> 2);
		}
		return array;
	}
}
