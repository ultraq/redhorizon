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

package nz.net.ultraq.redhorizon.engine.input

import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector3f
import org.joml.primitives.Rayf
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor

/**
 * A system for processing user input.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class InputSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(InputSystem)

	final InputEventHandler input
	final Window window
	private final Vector3f worldPosition = new Vector3f()
	private final List<Selectable> selectables = []
	private Rayf cursorRay = new Rayf()
	private Selectable selection

	@Override
	void update(Scene scene, float delta) {

		average('Update: {}ms', 1f, logger) { ->

			// Process registered control bindings
			input.processInputs()

			// Find if any nodes are selected by the cursor
			var cursor = input.cursorPosition()
			scene.find(Camera).unproject(window.viewport, cursor, worldPosition)
			cursorRay.set(worldPosition.x(), worldPosition.y(), 0f, 0f, 0f, -1f)
			selectables.clear()
			scene.findAll(Selectable, selectables).each { selectable ->
				if (selectable.intersectsRay(cursorRay)) {
					if (selectable == selection) {
						// Do nothing, we don't have a 'cursor stay' event
					}
					else {
						if (selection) {
							selection.trigger(new CursorExitEvent())
						}
						selection = selectable
						selectable.trigger(new CursorEnterEvent())
					}
				}
				else if (selectable == selection) {
					selection.trigger(new CursorExitEvent())
					selection = null
				}
			}
		}
	}
}
