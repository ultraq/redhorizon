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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.OverlayRenderPass
import nz.net.ultraq.redhorizon.engine.input.ControlAddedEvent
import nz.net.ultraq.redhorizon.engine.input.ControlRemovedEvent
import nz.net.ultraq.redhorizon.engine.input.InputEventStream

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiWindowFlags.*

/**
 * Small overlay for displaying registered controls in the application.
 *
 * @author Emanuel Rabina
 */
class ControlsOverlayRenderPass implements OverlayRenderPass {

	private List<String> controls = []
	private int controlsWindowSizeX = 350
	private int controlsWindowSizeY = 200

	ControlsOverlayRenderPass(InputEventStream inputEventStream) {

		inputEventStream.on(ControlAddedEvent) { event ->
			controls << event.control.toString()
		}
		inputEventStream.on(ControlRemovedEvent) { event ->
			controls.remove(event.control.toString())
		}
	}

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	@Override
	void render(GraphicsRenderer renderer, Framebuffer sceneFramebufferResult) {

		if (!controls) {
			return
		}

		def viewport = ImGui.getMainViewport()
		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos(viewport.sizeX - controlsWindowSizeX - 10 as float, viewport.sizeY - controlsWindowSizeY - 10 as float)

		ImGui.begin('Controls', new ImBoolean(true),
			NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
		controlsWindowSizeX = ImGui.getWindowSizeX() as int
		controlsWindowSizeY = ImGui.getWindowSizeY() as int

		controls.each { control ->
			ImGui.text(control)
		}

		ImGui.end()
	}
}
