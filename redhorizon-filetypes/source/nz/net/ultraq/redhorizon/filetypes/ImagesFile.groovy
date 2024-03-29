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
 * Interface for files which contain multiple images, but not to be played in
 * sequence like an animation.  The images all share the same dimensions and
 * colour format.
 *
 * @author Emanuel Rabina
 */
interface ImagesFile extends ImageBase, ResourceFile {

	/**
	 * Returns the image data for all of the images in this file.
	 *
	 * @return Image data for each image.
	 */
	ByteBuffer[] getImagesData()

	/**
	 * Returns the number of images in this file.
	 *
	 * @return Number of images.
	 */
	int getNumImages()
}
