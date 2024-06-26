/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer

import groovy.transform.TupleConstructor

/**
 * Available palette types.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor
enum PaletteType {

	// @formatter:off
	RA_TEMPERATE ('ra-temperate.pal'),
	RA_SNOW      ('ra-snow.pal'),
	RA_INTERIOR  ('ra-interior.pal'),
	TD_TEMPERATE ('td-temperate.pal'),
	TD_WINTER    ('td-winter.pal'),
	TD_DESERT    ('td-desert.pal')
	// @formatter:on

	final String file
}
