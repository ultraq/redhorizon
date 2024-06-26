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

package nz.net.ultraq.redhorizon.explorer.animation

/**
 * <a href="https://easings.net/">https://easings.net/</a>  Useful either by
 * themselves or to control a {@link Transition}
 *
 * @author Emanuel Rabina
 */
class EasingFunctions {

	/**
	 * Convenience method for a linear interpolation.
	 */
	static float linear(float x) {
		return x
	}

	/**
	 * <a href="https://easings.net/#easeInSine">https://easings.net/#easeInSine</a>
	 */
	static float easeInSine(float x) {
		return 1 - Math.cos((x * Math.PI) / 2)
	}

	/**
	 * <a href="https://easings.net/#easeInCubic">https://easings.net/#easeInCubic</a>
	 */
	static float easeInCubic(float x) {
		return x**3
	}

	/**
	 * <a href="https://easings.net/#easeOutCubic">https://easings.net/#easeOutCubic</a>
	 */
	static float easeOutCubic(float x) {
		return 1 - (1 - x)**3
	}
}
