/*
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.units

/**
 * Model of the data of a C&C unit type.
 *
 * @author Emanuel Rabina
 */
class UnitData {

	String type
	// TODO: Maybe don't need this shpFile nested part
	ShpFile shpFile

	static class ShpFile {
		ShpFileParts parts
		ShpFileState[] states = []

		/**
		 * Get the number of frames from prior states before the given state.
		 */
		int getStateFramesOffset(ShpFileState state) {

			var priorFrames = 0
			var stateIndex = states.findIndexOf { it.name == state.name }
			for (var i = 0; i < stateIndex; i++) {
				var priorStates = states[i]
				priorFrames += (priorStates.frames * priorStates.headings)
			}
			return priorFrames
		}
	}

	static class ShpFileParts {
		ShpFilePart body
		ShpFilePart bodyAlt
		ShpFilePart turret
	}

	static class ShpFilePart {
		String combineWith
		int headings = 1
	}

	static class ShpFileState {
		String name
		int frames = 1
		int headings = 1
	}
}
