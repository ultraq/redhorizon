/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer.ui

import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.engine.EngineLoopStartEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.OverlayRenderPass
import nz.net.ultraq.redhorizon.geometry.Dimension

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.*

/**
 * A Command & Conquer asset explorer, allows peeking into and previewing the
 * classic C&C files using a file explorer-like interface.
 * 
 * @author Emanuel Rabina
 */
class ExplorerWindow extends Application {

	/**
	 * Constructor, sets up an application with the default configurations.
	 */
	ExplorerWindow() {

		super(new AudioConfiguration(), new GraphicsConfiguration(
			debug: false,
			renderResolution: new Dimension(800, 500)
		))
	}

	@Override
	void run() {

		graphicsEngine.on(EngineLoopStartEvent) { event ->
			graphicsEngine.renderPipeline.addOverlayPass(new ExplorerGuiRenderPass())
		}
	}

	/**
	 * A render pass for drawing the ImGui explorer elements.
	 */
	private class ExplorerGuiRenderPass implements OverlayRenderPass {

		final boolean enabled = true

		@Override
		void render(GraphicsRenderer renderer) {

			def viewport = ImGui.getMainViewport()
			ImGui.setNextWindowPos(viewport.workPosX, viewport.workPosY)
			ImGui.setNextWindowSize(viewport.workSizeX, viewport.workSizeY)
			ImGui.pushStyleVar(WindowRounding, 0)
			ImGui.pushStyleVar(WindowBorderSize, 0)

			ImGui.begin('Dockspace', new ImBoolean(true),
				NoTitleBar | NoCollapse | NoResize | NoMove | NoBringToFrontOnFocus | NoNavFocus | MenuBar | NoDocking)
			ImGui.popStyleVar(2)

			ImGui.dockSpace(ImGui.getID('MyDockspace'))

			if (ImGui.beginMenuBar()) {
				if (ImGui.beginMenu('File')) {
					if (ImGui.menuItem('Exit')) {
						stop()
					}
					ImGui.endMenu()
				}
				ImGui.endMenuBar()
			}

			ImGui.end()

			ImGui.begin('Current directory', new ImBoolean(true))
			ImGui.text('Hello!')
			ImGui.end()
		}
	}
}
