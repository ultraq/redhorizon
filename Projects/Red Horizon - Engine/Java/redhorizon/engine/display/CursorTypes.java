
// ===============================================
// Scanner's Java - List of available cursor types
// ===============================================

package redhorizon.engine.display;

/**
 * Enumeration of available cursor types.  The {@link #propertyname} value
 * matches a property found in the <tt>GameWindow.properties</tt> file.
 * 
 * @author Emanuel Rabina
 */
public enum CursorTypes {

	DEFAULT        ("default",        0.0f, 0.0f),
	COMMAND_SELECT ("command-select", 0.5f, 0.5f),
	COMMAND_MOVE   ("command-move",   0.5f, 0.5f);

	final String propertyname;
	final float hotspotx;
	final float hotspoty;

	/**
	 * Constructor, sets the hotspot location of the cursor to it's cursor type.
	 * These locations are floating-point values (between 0 and 1) indicating
	 * where along the width/height of the image the hotspot is.
	 * 
	 * @param propertyname Name of the property for this cursor.
	 * @param hotspotx	   Hotspot X co-ordinate.
	 * @param hotspoty	   Hotspot Y co-ordinate.
	 */
	private CursorTypes(String propertyname, float hotspotx, float hotspoty) {

		this.propertyname = propertyname;
		this.hotspotx     = hotspotx;
		this.hotspoty     = hotspoty;
	}
}
