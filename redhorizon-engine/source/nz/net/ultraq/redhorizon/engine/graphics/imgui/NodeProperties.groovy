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

	private final HashMap<Node, List<Property<?>>> nodePublicPropertiesMap = new HashMap<>()

	final boolean debug = true

	/**
	 * Return a list of the public properties of the given node.
	 */
	private List<Property<?>> getPublicProperties(Node node) {

		return nodePublicPropertiesMap.getOrCreate(node) { ->
			var publicProperties = new ArrayList<Property<?>>()

			// Public fields
			node.class.declaredFields.each { Field field ->
				if (field.modifiers & Modifier.PUBLIC && !field.name.startsWith('__')) {
					publicProperties << new Property(field.name, field.type,
						{ -> field.get(node) },
						{ value -> field.set(node, value) })
				}
			}

			// Public properties following the JavaBean spec using getters/setters
			var declaredMethods = node.class.declaredMethods
			declaredMethods.each { Method method ->
				if (method.modifiers & Modifier.PUBLIC && method.name.startsWith('get')) {
					var setter = declaredMethods.find { it.name == method.name.replaceFirst('get', 'set') } as Method
					publicProperties << new Property(method.name.substring(3).uncapitalize(), method.returnType,
						{ -> method.invoke(node) },
						setter ? { value -> setter.invoke(node, value) } : null)
				}
			}

			return publicProperties
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
						var floats = new float[]{ property.getter() as float }
						var name = property.name
						if (!property.setter) {
							name += ' (read-only)'
						}
						ImGui.dragFloat(name, floats, 0.5f)
						if (property.setter && floats[0] != property.getter()) {
							property.setter(floats[0])
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
	private static class Property<T> {

		final String name
		final Class<T> type
		final Closure<T> getter
		final Closure setter
	}
}
