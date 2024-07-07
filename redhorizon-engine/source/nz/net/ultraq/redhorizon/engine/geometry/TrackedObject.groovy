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

package nz.net.ultraq.redhorizon.engine.geometry
/**
 * A wrapper around any object where we wish to control reads/writes and track
 * when a change has happened since some prior point.
 *
 * @author Emanuel Rabina
 */
class TrackedObject<T> {

	private final T value
	private boolean changed

	/**
	 * Constructor, wrap the value we want to track.
	 */
	TrackedObject(T value) {

		this.value = value
	}

	/**
	 * Return the underlying value.  Any modifications made to the returned object
	 * are untracked.
	 */
	T get() {

		return value
	}

	/**
	 * Return whether or not something has modified the underlying value via
	 * {@link #modify}
	 */
	boolean isChanged() {

		return changed
	}

	/**
	 * Make changes to the underlying object.  This will make the next call to
	 * {@link #isChanged} return {@code true}.
	 */
	void modify(@DelegatesTo(type = 'T') Closure closure) {

		closure.delegate = value
		closure()
		changed = true
	}

	/**
	 * Reset the {@code changed} state of this object.
	 */
	void reset() {

		changed = false
	}
}
