/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli.objectviewer.maps

import groovy.transform.TupleConstructor

/**
 * All of the overlay tiles and their matching name and hex values in the
 * scenario files.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor
enum MapRAOverlayPackTiles {

	// Walls
	WALL_SANDBAGS    ('SBAG', 0x00, true, false),
	WALL_CHAINLINK   ('CYCL', 0x01, true, false),
	WALL_CONCRETE    ('BRIK', 0x02, true, false),
	WALL_BARBEDWIRE  ('FENC', 0x17, true, false),
	WALL_WOODENFENCE ('WOOD', 0x04, true, false),

	// Ore & Gems
	ORE1  ('GOLD01', 0x05, false, true),
	ORE2  ('GOLD02', 0x06, false, true),
	ORE3  ('GOLD03', 0x07, false, true),
	ORE4  ('GOLD04', 0x08, false, true),
	GEMS1 ('GEM01', 0x09, false, true),
	GEMS2 ('GEM02', 0x0a, false, true),
	GEMS3 ('GEM03', 0x0b, false, true),
	GEMS4 ('GEM04', 0x0c, false, true),

	// Farm fields
	FIELD_HAYSTACKS ('V12', 0x0d, false, false),
	FIELD_HAYSTACK  ('V13', 0x0e, false, false),
	FIELD_WHEAT     ('V14', 0x0f, false, false),
	FIELD_FALLOW    ('V15', 0x10, false, false),
	FIELD_CORN      ('V16', 0x11, false, false),
	FIELD_CELERY    ('V17', 0x12, false, false),
	FIELD_POTATO    ('V18', 0x13, false, false),

	// Crates
	CRATE_WOOD   ('WCRATE', 0x15, false, false, true),
	CRATE_SILVER ('SCRATE', 0x16, false, false, true),
	CRATE_WATER  ('WWCRATE', 0x18, false, false, true),

	// Misc
	FLAGHOLDER ('FPLS', 0x14, false, false)

	// Tile attributes
	final String name
	final int value
	final boolean isWall
	final boolean isResource
	final boolean useShp = false
}
