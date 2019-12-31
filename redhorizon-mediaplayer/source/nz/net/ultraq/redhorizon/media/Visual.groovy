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
	 * Figure out what the image dimensions should be to fit comfortably within
	 * the given window.
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @param fixAspectRatio
	 * @param window
	 * @return
	 */
	Dimension calculateImageDimensionsForWindow(int imageWidth, int imageHeight, boolean fixAspectRatio, Dimension window) {

		def width = window.width
		def height = imageHeight * (width / imageWidth)
		if (fixAspectRatio) {
			height *= 1.2
		}
		return new Dimension(width, Math.ceil(height) as int)
	}

	/**
	 * Return a set of image coordinates centered around the origin for the given
	 * image size.
	 * 
	 * @param imageSize
	 * @return
	 */
	Rectanglef centerImageCoordinates(Dimension imageSize) {

		return (imageSize as Rectanglef).translate(-imageSize.width / 2, -imageSize.height / 2)
	}
}