
package redhorizon.utilities.converter.ui;

import nz.net.ultraq.preferences.UserPreferencesKey;

import java.util.ArrayList;

/**
 * Enum of preferences used by the SMART notice generator.
 * 
 * @author Emanuel rabina (sz0rcv)
 */
public enum ConverterUIPreferences implements UserPreferencesKey {

	WINDOW_MAXIMIZED     (false),
	WINDOW_BOUNDS_X      (0),
	WINDOW_BOUNDS_Y      (0),
	WINDOW_BOUNDS_WIDTH  (1000),
	WINDOW_BOUNDS_HEIGHT (750),

	CONVERSION_HISTORY (new ArrayList<Conversion>());

	private final Object defaultvalue;

	/**
	 * Constructor, set the default value if the preference is not available.
	 * 
	 * @param defaultvalue
	 */
	private ConverterUIPreferences(Object defaultvalue) {

		this.defaultvalue = defaultvalue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object defaultValue() {

		return defaultvalue;
	}
}
