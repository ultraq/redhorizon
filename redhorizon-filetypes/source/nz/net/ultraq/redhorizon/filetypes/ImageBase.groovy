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

package nz.net.ultraq.redhorizon.filetypes

/**
 * Common properties of all image-based file formats.
 * 
 * @author Emanuel Rabina
 */
interface ImageBase {

	/**
	 * Returns the number of bytes used to represent the colour data of a single
	 * pixel.  If the value is {@link ColourFormat#FORMAT_INDEXED}, then a palette
	 * needs to be applied to obtain a full colour image.
	 * 
	 * @return The image colour format.
	 */
	ColourFormat getFormat()

	/**
	 * Returns the height of the image.
	 * 
	 * @return Height of the image.
	 */
	int getHeight()

	/**
	 * Returns the width of the image.
	 * 
	 * @return Width of the image.
	 */
	int getWidth()

	/**
	 * Returns whether this visual format was developed for VGA monitors, and so
	 * needs an aspect ratio adjustment to look 'right' on modern screens.
	 * 
	 * @return
	 */
	default boolean isForVgaMonitors() {

		return false
	}
}
