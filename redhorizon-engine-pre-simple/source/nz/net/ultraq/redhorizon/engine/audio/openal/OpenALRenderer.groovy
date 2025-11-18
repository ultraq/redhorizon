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

package nz.net.ultraq.redhorizon.engine.audio.openal

import nz.net.ultraq.redhorizon.audio.AudioResource
import nz.net.ultraq.redhorizon.audio.Buffer
import nz.net.ultraq.redhorizon.audio.Source
import nz.net.ultraq.redhorizon.audio.openal.OpenALBuffer
import nz.net.ultraq.redhorizon.audio.openal.OpenALSource
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.audio.BufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.audio.BufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.audio.SourceCreatedEvent
import nz.net.ultraq.redhorizon.engine.audio.SourceDeletedEvent

import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.openal.AL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.openal.AL10.*
import static org.lwjgl.system.MemoryStack.stackPush

import java.lang.reflect.Modifier
import java.nio.ByteBuffer

/**
 * An audio renderer using the OpenAL API.
 *
 * @author Emanuel Rabina
 */
class OpenALRenderer implements AudioRenderer {

	private static Logger logger = LoggerFactory.getLogger(OpenALRenderer)

	private final AudioConfiguration config

	/**
	 * Constructor, create an OpenAL renderer with the given configuration.
	 */
	OpenALRenderer(AudioConfiguration config) {

		this.config = config
	}

	/**
	 * Check for any OpenAL errors created by the OpenAL call in the given
	 * closure, throwing them if they occur.
	 */
	private static <T> T checkForError(Closure<T> closure) {

		def result = closure()
		def error = alGetError()
		if (error != AL_NO_ERROR) {
			var errorCode = AL.getFields().find { field ->
				return Modifier.isStatic(field.modifiers) && field.name.startsWith("AL_") && field.getInt(null) == error
			}
			logger.error("OpenAL error: ${errorCode}")
		}
		return result
	}

	@Override
	void close() {
	}

	@Override
	Buffer createBuffer(int bits, int channels, int frequency, ByteBuffer data) {

		var buffer = new OpenALBuffer(bits, channels, frequency, data)
		trigger(new BufferCreatedEvent(buffer))
		return buffer
	}

	@Override
	Source createSource() {

		var source = new OpenALSource()
		trigger(new SourceCreatedEvent(source))
		return source
	}

	@Override
	void delete(AudioResource resource) {

		resource.close()
		switch (resource) {
			case Buffer -> trigger(new BufferDeletedEvent(resource))
			case Source -> trigger(new SourceDeletedEvent(resource))
			default -> throw new IllegalArgumentException("Cannot delete resource of type ${resource}")
		}
	}

	/**
	 * Emit some information about the OpenAL rendering device.
	 */
	@Override
	String toString() {

		return "OpenAL device: ${alGetString(AL_VENDOR)}, ${alGetString(AL_RENDERER)}, OpenAL ${alGetString(AL_VERSION)}"
	}

	@Override
	void updateListener(float gain, Vector3fc position) {

		stackPush().withCloseable { stack ->
			alListenerf(AL_GAIN, gain)
			alListenerfv(AL_POSITION, position.get(stack.mallocFloat(Vector3f.FLOATS)))
//			alListenerfv(AL_VELOCITY, velocity as float[])
//			alListenerfv(AL_ORIENTATION, orientation as float[])
		}
	}

	@Override
	void updateSource(Source source, Vector3fc position) {

		stackPush().withCloseable { stack ->
			alSourcefv(((OpenALSource)source).sourceId, AL_POSITION, position.get(stack.mallocFloat(Vector3f.FLOATS)))
//		checkForError { -> alSourcefv(sourceId, AL_DIRECTION, direction as float[]) }
//		checkForError { -> alSourcefv(sourceId, AL_VELOCITY, velocity as float[]) }
		}
	}
}
