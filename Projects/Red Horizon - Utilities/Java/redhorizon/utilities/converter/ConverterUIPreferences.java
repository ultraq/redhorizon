
package redhorizon.utilities.converter;

import nz.net.ultraq.preferences.UserPreferencesKey;

/**
 * Enum of preferences used by the SMART notice generator.
 * 
 * @author Emanuel rabina (sz0rcv)
 */
public enum ConverterUIPreferences implements UserPreferencesKey {

	WINDOW_MAXIMIZED     (false),
	WINDOW_BOUNDS_X      (0),
	WINDOW_BOUNDS_Y      (0),
	WINDOW_BOUNDS_WIDTH  (800),
	WINDOW_BOUNDS_HEIGHT (600);

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
