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

package nz.net.ultraq.redhorizon.engine

import nz.net.ultraq.redhorizon.scenegraph.Named

import groovy.transform.Memoized

/**
 * Any reusable behaviour that can be attached to an entity.
 *
 * @author Emanuel Rabina
 */
abstract class Component<T extends Component> implements Named<T> {

	protected Entity entity
	protected boolean enabled = true

	/**
	 * Disable this component.  The component will continue to exist attached to
	 * its entity, but will no longer participate in any systems.
	 */
	T disable() {

		enabled = false
		return (T)this
	}

	/**
	 * Enable this component.  The component will participate in any systems again.
	 */
	T enable() {

		enabled = true
		return (T)this
	}

	@Override
	@Memoized
	String getName() {

		if (hasCustomName()) {
			return Named.super.getName()
		}

		var sentenceCaseName = this.class.simpleName.toSentenceCase()
		return sentenceCaseName.substring(0, sentenceCaseName.lastIndexOf(' '))
	}

	/**
	 * Return whether this component is disabled.
	 */
	boolean isDisabled() {

		return !enabled
	}

	/**
	 * Return whether this component is enabled.
	 */
	boolean isEnabled() {

		return enabled
	}
}
