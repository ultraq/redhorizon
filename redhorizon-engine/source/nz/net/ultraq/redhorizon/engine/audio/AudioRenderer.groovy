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

package nz.net.ultraq.redhorizon.engine.audio

import nz.net.ultraq.redhorizon.geometry.Orientation
import nz.net.ultraq.redhorizon.geometry.Vector3f

/**
 * Interface for the audio renderer which is responsible for playing back sounds
 * through the audio device.
 * 
 * @author Emanuel Rabina
 */
interface AudioRenderer {

	/**
	 * Update details about the listener.
	 * 
	 * @param position
	 * @param velocity
	 * @param orientation
	 */
	void updateListener(Vector3f position, Vector3f velocity, Orientation orientation)
}
