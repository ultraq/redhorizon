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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule
import nz.net.ultraq.redhorizon.scenegraph.Node

import imgui.ImGui
import imgui.type.ImBoolean
import org.joml.Vector2f
import static imgui.flag.ImGuiCond.FirstUseEver

import groovy.transform.TupleConstructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * An ImGui panel showing the properties of the currently-selected node.
 *
 * @author Emanuel Rabina
 */
class NodeProperties extends ImGuiModule<NodeProperties> {

	private static final Class<?>[] supportedTypes = [float, Float, Vector2f]
	private final HashMap<Node, List<Property<?>>> nodePublicPropertiesMap = new LinkedHashMap<>()

	final boolean debug = true

	/**
	 * Return a list of the public properties of the given node.
	 */
	private List<Property<?>> getPublicProperties(Node node) {

		return nodePublicPropertiesMap.getOrCreate(node) { ->
			var publicProperties = new ArrayList<Property<?>>()

			// Public fields
			node.class.fields.each { Field field ->
				if (field.modifiers & Modifier.PUBLIC && !field.name.startsWith('__')) {
					var type = field.type
					if (type in supportedTypes) {
						publicProperties << new Property(field.name, type,
							{ -> field.get(node) },
							{ value -> field.set(node, value) })
					}
				}
			}

			// Public properties following the JavaBean spec using getters/setters
			var methods = node.class.methods
			methods.each { Method method ->
				if (method.modifiers & Modifier.PUBLIC && method.name.startsWith('get')) {
					var type = method.returnType
					if (type in supportedTypes) {
						var setter = methods.find { it.name == method.name.replaceFirst('get', 'set') } as Method
						publicProperties << new Property(method.name.substring(3).uncapitalize(), type,
							{ -> method.invoke(node) },
							setter ? { value -> setter.invoke(node, value) } :
								type == Vector2f ? { float[] values -> ((Vector2f)method.invoke(node)).set(values) } :
									null)
					}
				}
			}

			return publicProperties.sort()
		}
	}

	@Override
	void render(ImGuiContext context) {

		ImGui.setNextWindowSize(250, 400, FirstUseEver)
		ImGui.useWindow('Node properties', new ImBoolean(true)) { ->

			var selectedNode = scene.find(NodeList)?.selectedNode
			if (selectedNode) {
				getPublicProperties(selectedNode).each { property ->

					// Float values
					if (property.type == float || property.type == Float) {
						var floats = new float[]{ property.read() as float }
						if (ImGui.dragFloat("${property.name}${property.readOnly ? ' (read-only)' : ''}", floats, 0.1f)) {
							property.update(floats[0])
						}
					}
					// Vectors
					else if (property.type == Vector2f) {
						var vector = property.read() as Vector2f
						var floats = vector.get(new float[2])
						if (ImGui.dragFloat2(property.name, floats, 0.1f)) {
							property.update(floats)
						}
					}
				}
			}
		}
	}

	/**
	 * A class representing a public property that can be modified.  Includes the
	 * field name, type, value, and a setter function if it has one.
	 */
	@TupleConstructor(defaults = false)
	private static class Property<T> implements Comparable<Property<T>> {

		final String name
		final Class<T> type
		final Closure<T> getter
		final Closure setter

		@Override
		int compareTo(Property<T> o) {

			return name <=> o.name
		}

		/**
		 * Return whether the property is read-only.
		 */
		boolean isReadOnly() {

			return !setter
		}

		/**
		 * Read the value of the property.
		 */
		T read() {

			return getter()
		}

		/**
		 * Update the value of the property.  Does nothing if the property is
		 * read-only.
		 */
		void update(T value) {

			setter?.call(value)
		}
	}
}
