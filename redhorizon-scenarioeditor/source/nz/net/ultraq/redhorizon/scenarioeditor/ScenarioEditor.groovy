/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.scenarioeditor

import nz.net.ultraq.redhorizon.classic.filetypes.IniFile
import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * Application for viewing and editing classic Command & Conquer maps/missions.
 *
 * @author Emanuel Rabina
 */
class ScenarioEditor extends Application {

	private static final Logger logger = LoggerFactory.getLogger(ScenarioEditor)
	private static final Dimension renderResolution = new Dimension(1280, 800)

	private final IniFile mapFile
	private final Palette palette

	/**
	 * Constructor, sets up an application with the default configurations.
	 *
	 * @param version
	 * @param palette
	 */
	ScenarioEditor(String version, IniFile mapFile, Palette palette) {

		super("Red Horizon Scenario Editor ${version}",
			new AudioConfiguration(),
			new GraphicsConfiguration(
				renderResolution: renderResolution,
				startWithChrome: true
			)
		)

		this.mapFile = mapFile
		this.palette = palette
	}

	@Override
	protected void applicationStart() {

		def mouseMovementModifier = 1f
		graphicsSystem.on(WindowCreatedEvent) { event ->
			mouseMovementModifier = event.window.renderResolution.width / event.window.targetResolution.width
		}

		// Key event handler
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
					case GLFW_KEY_ESCAPE:
						stop()
						break
				}
			}
		}

		// Use click-and-drag to move around
		def cursorPosition = new Vector2f()
		def dragging = false
		inputEventStream.on(CursorPositionEvent) { event ->
			if (dragging) {
				def diffX = (cursorPosition.x - event.xPos) * mouseMovementModifier as float
				def diffY = (cursorPosition.y - event.yPos) * mouseMovementModifier as float
				graphicsSystem.camera.translate(-diffX, diffY)
			}
			cursorPosition.set(event.xPos as float, event.yPos as float)
		}
		inputEventStream.on(MouseButtonEvent) { event ->
			if (event.button == GLFW_MOUSE_BUTTON_LEFT) {
				if (event.action == GLFW_PRESS) {
					dragging = true
				}
				else if (event.action == GLFW_RELEASE) {
					dragging = false
				}
			}
		}

		// TODO: Load the map
	}
}
