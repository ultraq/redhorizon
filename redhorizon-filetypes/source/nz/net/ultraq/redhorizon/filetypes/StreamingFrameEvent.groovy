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

import nz.net.ultraq.redhorizon.events.Event

import groovy.transform.TupleConstructor
import java.nio.ByteBuffer

/**
 * Event for the streaming of a frame of image data.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class StreamingFrameEvent extends Event {

	final int width
	final int height
	final ColourFormat format
	final ByteBuffer frame

	/**
	 * Return the frame from this event already flipped vertically.
	 */
	ByteBuffer getFrameFlippedVertical() {

		return frame.flipVertical(width, height, format)
	}
}
