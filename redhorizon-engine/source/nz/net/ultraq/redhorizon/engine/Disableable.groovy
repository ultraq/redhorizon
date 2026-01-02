/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine

/**
 * Any object that can have a disabled state which should prevent it from
 * participating in any systems.
 *
 * @author Emanuel Rabina
 */
trait Disableable<T extends Disableable> {

	private boolean enabled = true

	/**
	 * Disable this object.
	 */
	T disable() {

		enabled = false
		return (T)this
	}

	/**
	 * Enable this object.
	 */
	T enable() {

		enabled = true
		return (T)this
	}

	/**
	 * Return whether this object is disabled.
	 */
	boolean isDisabled() {

		return !enabled
	}

	/**
	 * Return whether this object is enabled.
	 */
	boolean isEnabled() {

		return enabled
	}
}
