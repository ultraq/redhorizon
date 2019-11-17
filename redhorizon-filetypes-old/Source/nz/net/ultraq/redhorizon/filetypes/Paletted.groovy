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

import java.nio.channels.ReadableByteChannel

/**
 * Interface which identifies the file format as using indexed data to represent
 * the image information returned by the implementing class.  Thus requiring a
 * matching palette to obtain the 'whole' image.
 * 
 * @author Emanuel Rabina
 */
interface Paletted extends ImageCommon {

	/**
	 * Returns the raw indexed data which constructs this file's image.
	 * 
	 * @return The indexed image data.
	 */
	ReadableByteChannel getRawImageData()
}