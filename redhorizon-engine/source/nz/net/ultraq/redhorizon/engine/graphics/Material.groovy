/*
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

/**
 * A material defines special properties to how a shape should be rendered.
 * These are often uniform values that go into configuring a shader, stored as a
 * general-purpose {@code Map}, with extensions used to simplify access to that
 * map.
 *
 * @author Emanuel Rabina
 */
class Material {

	private static final String KEY_FRAME = 'frame'
	private static final String KEY_FRAMES_HORIZONTAL = 'framesHorizontal'
	private static final String KEY_FRAMES_VERTICAL = 'framesVertical'
	private static final String KEY_FRAME_STEP_X = 'frameStepX'
	private static final String KEY_FRAME_STEP_Y = 'frameStepY'

	static final String[] KEYS_SPRITES = [
		KEY_FRAME, KEY_FRAMES_HORIZONTAL, KEY_FRAMES_VERTICAL, KEY_FRAME_STEP_X, KEY_FRAME_STEP_Y
	]

	final Map<String, Object> attributes = [:]
	Texture texture

	void setFrame(int frame) {
		attributes[KEY_FRAME] = frame
	}

	void setFrameStepX(float frameStepX) {
		attributes[KEY_FRAME_STEP_X] = frameStepX
	}

	void setFrameStepY(float frameStepY) {
		attributes[KEY_FRAME_STEP_Y] = frameStepY
	}

	void setFramesHorizontal(int framesHorizontal) {
		attributes[KEY_FRAMES_HORIZONTAL] = framesHorizontal
	}

	void setFramesVertical(int framesVertical) {
		attributes[KEY_FRAMES_VERTICAL] = framesVertical
	}
}
