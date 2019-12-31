/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.media

import org.joml.Rectanglef

/**
 * Trait for media players displaying visual content.
 * 
 * @author Emanuel Rabina
 */
trait Visual {

	/**
	 * Calculate the ideal centered dimensions for the image that stretches to fit
	 * the window while maintaining the target aspect ratio.
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @param fixAspectRatio
	 * @param window
	 * @return
	 */
	Rectanglef calculateCenteredDimensions(int imageWidth, int imageHeight, boolean fixAspectRatio, Dimension window) {

		def width = window.width
		def height = imageHeight * (width / imageWidth)
		if (fixAspectRatio) {
			height *= 1.2
		}
		return new Rectanglef(0, 0, width, height).translate(-width / 2, -height / 2)
	}
}
