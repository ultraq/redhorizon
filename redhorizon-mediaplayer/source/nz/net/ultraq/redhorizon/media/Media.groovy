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

package nz.net.ultraq.redhorizon.media

import java.util.concurrent.atomic.AtomicInteger

/**
 * Base class for all media classes in this package.
 * 
 * @author Emanuel Rabina
 */
abstract class Media {

	public final String name
	private static final AtomicInteger id = new AtomicInteger()

	/**
	 * Constructor, assigns a name to this media object.
	 * 
	 * @param name Name to assign this media object.
	 */
	protected Media(String name) {

		this.name = name + id.getAndIncrement()
	}

	/**
	 * Checks if this media item is the same as another.
	 * 
	 * @param obj
	 * @return {@code true} if both media objects are of the same type and name.
	 */
	@Override
	boolean equals(Object obj) {

		return obj instanceof Media && name == obj.name
	}
}
