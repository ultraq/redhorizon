/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.runtime.utilities

import nz.net.ultraq.redhorizon.graphics.Colour

import picocli.CommandLine.ITypeConverter

/**
 * Let Picocli understand the {@link Colour} class.
 *
 * @author Emanuel Rabina
 */
class ColourTypeConverter implements ITypeConverter<Colour> {

	private static final String COLOUR_NAME = 'Background colour'

	@Override
	Colour convert(String value) throws Exception {

		// Value is a hex code
		if (value.startsWith('#')) {
			return new Colour(COLOUR_NAME,
				Integer.parseInt(value.substring(1, 3), 16) / 256f as float,
				Integer.parseInt(value.substring(3, 5), 16) / 256f as float,
				Integer.parseInt(value.substring(5, 7), 16) / 256f as float
			)
		}

		// Value is 3 colour values
		if (value.contains(',')) {
			if (value.contains('f')) {
				var components = value.split(',').collect { Float.parseFloat(it.trim()) }
				return new Colour(COLOUR_NAME, components[0], components[1], components[2])
			}
			var components = value.split(',').collect { Integer.parseInt(it.trim()) }
			return new Colour(COLOUR_NAME,
				components[0] / 256f as float,
				components[1] / 256f as float,
				components[2] / 256f as float
			)
		}

		// Value is an enum identifier
		return (Colour)Colour.getDeclaredField(value.toUpperCase()).get(null)
	}
}
