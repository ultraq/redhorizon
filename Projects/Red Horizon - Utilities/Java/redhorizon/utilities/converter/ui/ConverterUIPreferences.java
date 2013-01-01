/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
