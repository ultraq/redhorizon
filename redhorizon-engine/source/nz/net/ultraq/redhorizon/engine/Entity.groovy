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

import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.SceneVisitor

/**
 * Any object in the scene that should be updated periodically.
 *
 * @author Emanuel Rabina
 */
class Entity<T extends Entity> extends Node<T> implements AutoCloseable {

	private List<Component> components = []

	/**
	 * Add a component to this entity.
	 */
	T addComponent(Component component) {

		components << component
		component.parent = this
		return (T)this
	}

	/**
	 * Add a component to this entity, and return the component.
	 */
	<TComponent extends Component> TComponent addAndReturnComponent(TComponent component) {

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
	 * Return the first component that matches the given predicate.
	 */
	<T extends Component> T findComponent(Closure predicate) {

		return (T)components.find(predicate)
	}

	/**
	 * Return all components that match the given predicate.
	 */
	<T extends Component> List<T> findComponents(Closure predicate) {

		return (List<T>)components.findAll(predicate)
	}

	@Override
	void traverse(SceneVisitor visitor) {

		visitor.visit(this)
		components.each { component ->
			visitor.visit(component)
		}
		children*.traverse(visitor)
	}
}
