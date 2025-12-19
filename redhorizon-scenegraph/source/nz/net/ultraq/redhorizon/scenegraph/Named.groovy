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

package nz.net.ultraq.redhorizon.scenegraph

/**
 * Any object in a scene that has a displayable name for debug purposes.
 *
 * @author Emanuel Rabina
 */
trait Named<T extends Named> {

	private String name

	/**
	 * Returns this object's name, defaulting to the class name if not set using
	 * {@link #withName(String)}.
	 */
	String getName() {

		return name ?: this.class.simpleName
	}

	/**
	 * Return whether or not a custom name has been set for this object.
	 * @return
	 */
	boolean hasCustomName() {

		return name != null
	}

	/**
	 * Set the name of this object.
	 */
	T withName(String name) {

		this.name = name
		return (T)this
	}
}
