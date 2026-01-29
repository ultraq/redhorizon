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

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.scenegraph.Node

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * Any object in the scene.
 *
 * @author Emanuel Rabina
 */
class Entity<T extends Entity<T>> extends Node<T> implements EventTarget<T>, AutoCloseable {

	private List<Component> components = []

	/**
	 * Add a component to this entity.
	 */
	T addComponent(Component component) {

		components << component
		component.entity = this
		return (T)this
	}

	/**
	 * Add a component to this entity, and return the component.
	 */
	<T extends Component> T addAndReturnComponent(T component) {

		addComponent(component)
		return component
	}

	@Override
	void close() {

		components.each { component ->
			if (component instanceof AutoCloseable) {
				component.close()
			}
		}
	}

	/**
	 * Change the state of all components to be disabled.
	 */
	T disable() {

		components*.disable()
		return (T)this
	}

	/**
	 * Change the state of all components to be enabled.
	 */
	T enable() {

		components*.enable()
		return (T)this
	}

	/**
	 * Return the first component that matches the given predicate.
	 */
	<T extends Component> T findComponent(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.Component') Closure predicate) {

		return (T)components.find(predicate)
	}

	/**
	 * Return the first component of the given class.
	 */
	<T extends Component> T findComponentByType(Class<T> type) {

		return findComponent { type.isInstance(it) }
	}

	/**
	 * Return all components of the given class.
	 *
	 * @param type
	 * @param results
	 *   If provided, then the matching components will be appended to this list.
	 *   Use this parameter to avoid the list allocation normally created by this
	 *   method.
	 */
	<T extends Component> List<T> findComponentsByType(Class<T> type, List<T> results = []) {

		components.each { component ->
			if (type.isInstance(component)) {
				results << (T)component
			}
		}
		return results
	}

	/**
	 * Remove a component from this entity.
	 */
	T removeComponent(Component component) {

		components.remove(component)
		component.entity = null
		return (T)this
	}
}
