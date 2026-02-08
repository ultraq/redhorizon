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

package nz.net.ultraq.redhorizon.explorer

import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.explorer.actions.CyclePaletteAction
import nz.net.ultraq.redhorizon.explorer.mixdata.MixDatabase
import nz.net.ultraq.redhorizon.explorer.objects.GlobalPalette
import nz.net.ultraq.redhorizon.explorer.previews.PreviewController
import nz.net.ultraq.redhorizon.explorer.ui.EntryList
import nz.net.ultraq.redhorizon.explorer.ui.MainMenuBar
import nz.net.ultraq.redhorizon.explorer.ui.UiController
import nz.net.ultraq.redhorizon.explorer.ui.UiSettingsComponent
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.input.KeyBinding
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene

import static org.lwjgl.glfw.GLFW.GLFW_KEY_P

/**
 * Explorer UI and preview area.
 *
 * @author Emanuel Rabina
 */
class ExplorerScene extends Scene {

	private static final Colour GRID_LINES_GREY = new Colour('GridLines-Grey', 0.6f, 0.6f, 0.6f)
	private static final Colour GRID_LINES_DARK_GREY = new Colour('GridLines-DarkGrey', 0.2f, 0.2f, 0.2f)

	final Window window
	final Camera camera
	final GridLines gridLines

	/**
	 * Constructor, create the initial scene (blank, unless asked to load a file
	 * at startup).
	 */
	ExplorerScene(Window window, InputEventHandler input, int width, int height, boolean touchpadInput,
		File startingDirectory, MixDatabase mixDatabase) {

		this.window = window

		camera = addAndReturnChild(new Camera(width, height, window))

		addChild(new Node()
			.withName('UI')
			.addChild(new DebugOverlay()
				.withCursorTracking(window, camera)
				.withProfilingLogging())
			.addChild(new MainMenuBar(window, this, touchpadInput)
				.withName('Main menu'))
			.addChild(new EntryList()
				.withName('Entry list'))
			.addChild(new NodeList(this)
				.withName('Node list'))
			.addChild(new LogPanel()
				.withName('Log panel'))
			.addChild(new UiSettingsComponent(startingDirectory, mixDatabase, touchpadInput)
				.withName('UI settings'))
			.addChild(new ScriptNode(UiController))
		)

		addChild(new Node()
			.withName('Preview controller')
			.addChild(new ScriptNode(PreviewController))
		)

		gridLines = addAndReturnChild(new GridLines(nz.net.ultraq.redhorizon.classic.maps.Map.MAX_BOUNDS, 24,
			GRID_LINES_DARK_GREY, GRID_LINES_GREY)
			.withName('Grid lines'))

		addChild(new GlobalPalette()
			.withName('Global palette & alpha mask'))

		input.addInputBinding(new KeyBinding(GLFW_KEY_P, true, { ->
			new CyclePaletteAction(this).cyclePalette()
		}))
	}
}
