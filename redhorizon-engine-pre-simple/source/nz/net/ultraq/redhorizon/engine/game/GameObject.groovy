/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.game

/**
 * Any object in the scene that should be updated by the {@link GameLogicSystem}.
 *
 * @author Emanuel Rabina
 */
interface GameObject {

	/**
	 * Called regularly by the game logic system, allowing it to perform any
	 * processing as a response to changes in the scene.
	 *
	 * @param delta
	 *   Time, in seconds, since the last time this method was called.
	 */
	void update(float delta)
}
