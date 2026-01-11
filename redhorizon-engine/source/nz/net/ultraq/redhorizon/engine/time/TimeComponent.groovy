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

package nz.net.ultraq.redhorizon.engine.time

import nz.net.ultraq.redhorizon.engine.Component

/**
 * A component for anything that can be affected by the flow of time.
 *
 * @author Emanuel Rabina
 */
interface TimeComponent<T extends TimeComponent> extends Component<T> {

	/**
	 * Perform any logic as part of the scene update.
	 *
	 * @param delta
	 *   The time elapsed here can differ from actual time if time for the scene
	 *   is changed in some way, eg: paused, or slowed down.
	 */
	void update(float delta)
}
