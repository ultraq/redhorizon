/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.audio

/**
 * The audio interface through which audio can be played.  One must first be
 * created before any other audio operations can be performed.
 *
 * <p>When a new audio device is created, the thread on which it was created is
 * made "current", meaning audio can only be played on that thread.  To play
 * audio from a different thread, call {@link #makeCurrent}, your commands, then
 * {@link #releaseCurrent}.  Alternatively, use the {@link #withCurrent} method
 * to wrap any closure passed to it with the make/release calls automatically.
 *
 * @author Emanuel Rabina
 */
interface Device extends AutoCloseable {

	/**
	 * Makes the context current on the executing thread.
	 */
	void makeCurrent()

	/**
	 * Releases the context that is current on the executing thread.
	 */
	void releaseCurrent()

	/**
	 * Surround the given closure with calls to {@link #makeCurrent} and
	 * {@link #releaseCurrent} so that audio commands can be executed in the
	 * current thread.
	 */
	default <T> T withCurrent(Closure<T> closure) {

		try {
			makeCurrent()
			return closure()
		}
		finally {
			releaseCurrent()
		}
	}
}
