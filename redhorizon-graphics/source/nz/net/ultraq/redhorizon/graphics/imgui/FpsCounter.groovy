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

package nz.net.ultraq.redhorizon.graphics.imgui

import nz.net.ultraq.redhorizon.graphics.GraphicsResource
import nz.net.ultraq.redhorizon.graphics.Window

import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImBoolean
import static imgui.flag.ImGuiConfigFlags.*
import static imgui.flag.ImGuiWindowFlags.*

/**
 * A basic FPS counter overlay build using ImGui.
 *
 * @author Emanuel Rabina
 */
class FpsCounter implements GraphicsResource {

	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw
	private final ImFont robotoFont
	private final ImFont robotoMonoFont
	private int width = 300

	/**
	 * Constructor, create a new FPS counter tied to an existing window.
	 */
	FpsCounter(Window window) {

		imGuiGlfw = new ImGuiImplGlfw()
		imGuiGl3 = new ImGuiImplGl3()
		ImGui.createContext()

		var io = ImGui.getIO()
		io.setConfigFlags(DockingEnable)

		var fontConfig1 = new ImFontConfig()
		robotoFont = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/imgui/Roboto-Medium.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, 20, fontConfig1)
		}
		fontConfig1.destroy()
		io.setFontDefault(robotoFont)

		var fontConfig2 = new ImFontConfig()
		robotoMonoFont = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/imgui/RobotoMono-Medium.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, 20, fontConfig2)
		}
		fontConfig2.destroy()

		imGuiGlfw.init(window.handle, true)
		imGuiGl3.init('#version 410 core')
	}

	@Override
	void close() {

		imGuiGl3.shutdown()
		imGuiGlfw.shutdown()
		ImGui.destroyContext()
	}

	/**
	 * Automatically mark the beginning and end of a frame as before and after the
	 * execution of the given closure.
	 */
	void withFrame(Closure closure) {

		imGuiGl3.newFrame()
		imGuiGlfw.newFrame()
		ImGui.newFrame()

		closure()

		var viewport = ImGui.getMainViewport()
		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos((float)(viewport.sizeX - width), viewport.workPosY)
		ImGui.pushFont(robotoMonoFont)

		ImGui.begin('Debug overlay', new ImBoolean(true), NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
		width = (int)ImGui.getWindowSizeX()
		var framerate = ImGui.getIO().framerate
		ImGui.text("FPS: ${sprintf('%.1f', framerate)}, ${sprintf('%.1f', 1000 / framerate)}ms")
		ImGui.end()

		ImGui.popFont()

		ImGui.render()
		imGuiGl3.renderDrawData(ImGui.getDrawData())
	}
}
