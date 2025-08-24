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

package nz.net.ultraq.redhorizon.filetypes

import nz.net.ultraq.eventhorizon.EventTarget

import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.RunnableFuture

/**
 * A special kind of {@link RunnableFuture} for decoding file data and emitting
 * the results as {@link StreamingEvent}s.  Decoders can be stopped using the
 * standard {@link Future#cancel} method, and the underlying {@code Runnable}
 * implementation must respect these controls.
 *
 * @author Emanuel Rabina
 */
class StreamingDecoder extends FutureTask<Void> implements EventTarget<StreamingDecoder> {

	/**
	 * Constructor, create a decoder with the underlying {@code Runnable},
	 * relaying events to this decoder.
	 */
	StreamingDecoder(Runnable runnable) {

		super(runnable, null)
		assert runnable instanceof EventTarget
		runnable.relay(StreamingEvent, this)
	}
}
