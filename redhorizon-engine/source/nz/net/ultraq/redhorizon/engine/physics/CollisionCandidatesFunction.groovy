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

package nz.net.ultraq.redhorizon.engine.physics

import nz.net.ultraq.redhorizon.physics.Collider
import nz.net.ultraq.redhorizon.scenegraph.Scene

/**
 * A function that calculates between which collider pairs a collision check
 * should be performed.
 *
 * @author Emanuel Rabina
 */
@FunctionalInterface
interface CollisionCandidatesFunction {

	/**
	 * Given the scene, return a list of pairs of colliders for which collision
	 * checks should be performed.
	 *
	 * @param scene
	 * @param results
	 *   The list into which the collision pairs should be added.
	 * @return
	 *   The {@code results} list.
	 */
	List<Tuple2<Collider, Collider>> calculate(Scene scene, List<Tuple2<Collider, Collider>> results)
}
