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

import java.nio.ByteBuffer

/**
 * Interface for images containing an internal palette.
 *
 * @author Emanuel Rabina
 */
interface InternalPalette {

	/**
	 * Retrieves the internal palette.
	 *
	 * @return An image file's internal palette.
	 */
	Palette getPalette()

	/**
	 * Retrieves the index image data, ie: before a palette is applied to produce
	 * a colour image.
	 *
	 * @return An image file's index image data
	 */
	ByteBuffer getIndexData()
}
