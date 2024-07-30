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

package nz.net.ultraq.redhorizon.classic.nodes

import groovy.transform.TupleConstructor

/**
 * For selecting a 2D layer in the scene to live on.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor
enum Layer {

	// @formatter:off

	// Absolute
	MAP_BACKGROUND (0.0),
	MAP_PACK       (0.1),
	STRUCTURE_BIBS (0.2),
	OVERLAY_PACK   (0.3),
	SPRITES_LOWER  (0.4),
	SPRITES        (0.5),
	SPRITES_UPPER  (0.6),
	OVERLAY        (1.0),

	// Relative
	UP_ONE      (0.1),
	CURRENT     (0.0),
	DOWN_ONE   (-0.1),
	DOWN_TWO   (-0.2),
	DOWN_THREE (-0.3)

	// @formatter:on

	final float value
}
