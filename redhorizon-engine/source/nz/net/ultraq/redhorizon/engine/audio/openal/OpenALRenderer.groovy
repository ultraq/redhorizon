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

import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.audio.Buffer
import nz.net.ultraq.redhorizon.engine.audio.BufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.audio.BufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.audio.Source
import nz.net.ultraq.redhorizon.engine.audio.SourceCreatedEvent
import nz.net.ultraq.redhorizon.engine.audio.SourceDeletedEvent
import nz.net.ultraq.redhorizon.engine.geometry.Orientation

import org.joml.Vector3f
import org.lwjgl.openal.AL
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

	private final AudioConfiguration config

	/**
	 * Constructor, create an OpenAL renderer with the given configuration.
	 *
	 * @param config
	 */
	OpenALRenderer(AudioConfiguration config) {

		this.config = config

		alListenerf(AL_GAIN, config.volume)
	}

	/**
	 * Check for any OpenAL errors created by the OpenAL call in the given
	 * closure, throwing them if they occur.
	 *
	 * @param closure
	 */
	private static <T> T checkForError(Closure<T> closure) {

		def result = closure()
		def error = alGetError()
		if (error != AL_NO_ERROR) {
			var errorCode = AL.getFields().find { field ->
				return Modifier.isStatic(field.modifiers) && field.name.startsWith("AL_") && field.getInt(null) == error
			}
			throw new Exception("OpenAL error: ${errorCode}")
		}
		return result
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
	void deleteBuffers(Buffer... buffers) {

		buffers.each { buffer ->
			buffer.close()
			trigger(new BufferDeletedEvent(buffer))
		}
	}

	@Override
	void deleteSource(Source source) {

		source.close()
		trigger(new SourceDeletedEvent(source))
	}

	/**
	 * Emit some information about the OpenAL rendering device.
	 *
	 * @return
	 */
	@Override
	String toString() {

		return """
			OpenAL audio renderer
			 - Vendor: ${alGetString(AL_VENDOR)}
			 - Device name: ${alGetString(AL_RENDERER)}
			 - OpenAL version: ${alGetString(AL_VERSION)}
		""".stripIndent()
	}

	@Override
	void updateListener(Vector3f position, Vector3f velocity, Orientation orientation) {

		stackPush().withCloseable { stack ->
			checkForError { -> alListenerfv(AL_POSITION, position.get(stack.mallocFloat(Vector3f.FLOATS))) }
//		checkForError { -> alListenerfv(AL_VELOCITY, velocity as float[]) }
//		checkForError { -> alListenerfv(AL_ORIENTATION, orientation as float[]) }
		}
	}

	@Override
	void updateSource(int sourceId, Vector3f position, Vector3f direction, Vector3f velocity) {

		stackPush().withCloseable { stack ->
			checkForError { -> alSourcefv(sourceId, AL_POSITION, position.get(stack.mallocFloat(Vector3f.FLOATS))) }
//		checkForError { -> alSourcefv(sourceId, AL_DIRECTION, direction as float[]) }
//		checkForError { -> alSourcefv(sourceId, AL_VELOCITY, velocity as float[]) }
		}
	}

	@Override
	void updateVolume(float volume) {

		checkForError { -> alListenerf(AL_GAIN, volume) }
	}
}
