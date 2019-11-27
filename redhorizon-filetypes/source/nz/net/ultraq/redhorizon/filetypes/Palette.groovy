/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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
 * Interface for data representing a colour palette.
 * 
 * @author Emanuel Rabina
 */
interface Palette {

	/**
	 * Return the colour data at the specified index.
	 * 
	 * @param index Position in the palette.
	 * @return <tt>byte</tt> array of the RGB(A) values of the requested colour.
	 */
	byte[] getColour(int index)

	/**
	 * Colour format used by this palette.
	 * 
	 * @return Palette colour format, RGB(A).
	 */
	ColourFormat getFormat()

	/**
	 * The number of colours in the palette.
	 * 
	 * @return Number of colours.
	 */
	int getSize()
}
