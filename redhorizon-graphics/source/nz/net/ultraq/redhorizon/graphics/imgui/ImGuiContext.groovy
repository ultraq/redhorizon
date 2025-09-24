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

import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import static imgui.flag.ImGuiConfigFlags.*

/**
 * Setup necessary to include ImGui in the rendering pipeline.
 *
 * @author Emanuel Rabina
 */
class ImGuiContext implements GraphicsResource {

	final ImFont robotoFont
	final ImFont robotoMonoFont
	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw

	/**
	 * Constructor, set up ImGui for the given window.
	 */
	ImGuiContext(long windowHandle) {

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

		imGuiGlfw.init(windowHandle, true)
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

		ImGui.render()
		imGuiGl3.renderDrawData(ImGui.getDrawData())
	}
}
