/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import java.util.concurrent.ExecutorService

/**
 * Interface for animation filetypes.
 * 
 * @author Emanuel Rabina
 */
interface AnimationFile {

	/**
	 * Returns the number of bytes used to represent the colour data of a single
	 * pixel.
	 * <p>
	 * If the object implements the {@link Paletted} interface, then the return
	 * value of this method is more of an expectation of the colour-depth,
	 * rather than a given.
	 * 
	 * @return The image colour format.
	 */
	ColourFormat getFormat()

	/**
	 * Get the uncompressed data for each frame of the animation that can be used
	 * for rendering.
	 * 
	 * @param executorService
	 *   Executor that can be used for running the streaming data worker in its
	 *   own thread, if this file is of a streaming nature.  Can be {@code null}
	 *   otherwise.
	 * @return Buffer for each frame.
	 */
	ByteBuffer[] getFrameData(ExecutorService executorService)

	/**
	 * Returns the speed at which this animation should be run, in
	 * frames-per-second.
	 * 
	 * @return
	 */
	float getFrameRate()

	/**
	 * Returns the height of the image.
	 * 
	 * @return Height of the image.
	 */
	int getHeight()

	/**
	 * Return the number of frames that make up this animation.
	 * 
	 * @return
	 */
	int getNumFrames()

	/**
	 * Returns the width of the image.
	 * 
	 * @return Width of the image.
	 */
	int getWidth()
}
