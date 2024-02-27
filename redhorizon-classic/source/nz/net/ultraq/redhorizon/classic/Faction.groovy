/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic

/**
 * Available factions in classic C&C.
 *
 * @author Emanuel Rabina
 */
@SuppressWarnings('GroovyAssignabilityCheck')
enum Faction {

	// @formatter:off
	GOLD  ([80,  81,  82,  83,  84,  85,  86,  87,  88,  89,  90,  91 , 92 , 93,  94,  95]),
	BLUE  ([160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175]),
	RED   ([229, 230, 231, 232, 233, 234, 235,   8, 236, 237, 238, 239, 221, 222, 223, 223]),
	GREEN ([208, 208, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 154, 155, 143]),
	ORANGE([208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223]),
	BROWN ([128, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 121, 122, 122, 123, 123]),
	TEAL  ([224, 224, 225, 225, 226, 184, 185, 186, 187, 188, 188, 189, 190, 190, 191, 191]),
	MAROON([200, 200, 201, 202, 203, 203, 204, 205, 206, 206, 207, 221, 222, 222, 223, 223])
	// @formatter:on

	final int[] colours

	private Faction(String name, int ordinal, List<Integer> colours) {
		super(name, ordinal)
		this.colours = colours as int[]
	}
}
