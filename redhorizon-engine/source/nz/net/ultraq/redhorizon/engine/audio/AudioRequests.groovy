/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.audio

import groovy.transform.ImmutableOptions
import java.nio.ByteBuffer
import java.util.concurrent.Future

/**
 * Interface to make requests of the audio system.
 *
 * @author Emanuel Rabina
 */
interface AudioRequests {

	static interface Request<T extends AudioResource> {}

	static record SourceRequest() implements Request<Source> {}

	@ImmutableOptions(knownImmutables = ['data'])
	static record BufferRequest(int bits, int channels, int frequency, ByteBuffer data) implements Request<Buffer> {}

	/**
	 * Request the creation or retrieval of the given resource type from the audio
	 * system, which will eventually be resolved in the returned {@code Future}.
	 */
	<V extends AudioResource, R extends Request<V>> Future<V> requestCreateOrGet(R request)

	/**
	 * Request the audio system to delete a resource.
	 */
	void requestDelete(AudioResource... resources)
}
