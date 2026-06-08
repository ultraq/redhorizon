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

package nz.net.ultraq.redhorizon.engine.scene

import nz.net.ultraq.redhorizon.engine.debug.DebugEverythingBinding
import nz.net.ultraq.redhorizon.engine.debug.DebugStore
import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.primitives.Rectanglef

/**
 * Additional helper methods for the {@link Scene} class.
 *
 * @author Emanuel Rabina
 */
class SceneExtensions {

	private static final GridLines DEFAULT_GRID_LINES = new GridLines(new Rectanglef(0f, 0f, 1000f, 1000f).center(), 50f,
		new Colour('Light grey', 0.85f, 0.85f, 0.85f, 1f), Colour.GREY)

	/**
	 * Add all of the debug components in one go.
	 */
	static <T extends Scene> T addDebugComponents(T self, Window window, Camera camera, InputEventHandler inputEventHandler,
		GridLines gridLines = DEFAULT_GRID_LINES) {

		// Find the first node and add all of the following debug information before it
		var firstNode = self.root.children.first()

		self.insertBefore(new DebugStore(), firstNode)
		self.insertBefore(
			gridLines
				.withName('Grid lines')
				.disable(),
			firstNode)
		var debugOverlay = new DebugOverlay()
			.withCursorTracking(window, camera)
			.withProfilingLogging()
			.disable()
		var nodeListComponent = new NodeList(self)
			.disable()
		var logPanelComponent = new LogPanel()
			.disable()
		self.insertBefore(
			new Node()
				.addChild(debugOverlay)
				.addChild(nodeListComponent)
				.addChild(logPanelComponent)
				.withName('Debug UI'),
			firstNode)

		var debugEverythingBinding = new DebugEverythingBinding(self)
		inputEventHandler
			.addImGuiOverlayBinding([debugOverlay])
			.addInputBinding(debugEverythingBinding)

		return self
	}
}
