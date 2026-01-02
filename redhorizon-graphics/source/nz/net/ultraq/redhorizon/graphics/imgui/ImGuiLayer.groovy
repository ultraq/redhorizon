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
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow

import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import org.joml.primitives.Rectanglei
import static imgui.flag.ImGuiConfigFlags.DockingEnable

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * ImGui setup and teardown so that we can include ImGui in the rendering
 * pipeline.
 *
 * @author Emanuel Rabina
 */
class ImGuiLayer implements ImGuiContext, GraphicsResource {

	final ImFont defaultFont
	final ImFont monospaceFont
	final GameWindow gameWindow
	private final OpenGLWindow window
	private int dockspaceId
	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw
	private final Rectanglei uiArea = new Rectanglei()

	/**
	 * Constructor, set up ImGui for the given window.
	 */
	ImGuiLayer(OpenGLWindow window, long windowHandle) {

		this.window = window

		imGuiGlfw = new ImGuiImplGlfw()
		imGuiGl3 = new ImGuiImplGl3()
		ImGui.createContext()

		var io = ImGui.getIO()
		io.setConfigFlags(DockingEnable)

		var fontConfig1 = new ImFontConfig()
		defaultFont = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/imgui/Roboto-Medium.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, Math.round(16 * uiScale), fontConfig1)
		}
		fontConfig1.destroy()
		io.setFontDefault(defaultFont)

		var fontConfig2 = new ImFontConfig()
		monospaceFont = getResourceAsStream('nz/net/ultraq/redhorizon/graphics/imgui/RobotoMono-Medium.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, Math.round(16 * uiScale), fontConfig2)
		}
		fontConfig2.destroy()

		imGuiGlfw.init(windowHandle, true)
		imGuiGl3.init('#version 410 core')

		ImGui.style.scaleAllSizes(uiScale)
		gameWindow = new GameWindow()
	}

	@Override
	void close() {

		imGuiGl3.shutdown()
		imGuiGlfw.shutdown()
		ImGui.destroyContext()
	}

	@Override
	int getDockspaceId() {

		return dockspaceId
	}

	@Override
	Rectanglei getUiArea() {

		return dockspaceId ?
			uiArea
				.setMin(gameWindow.lastImageX as int, gameWindow.lastImageY as int)
				.setLengths(gameWindow.lastImageWidth as int, gameWindow.lastImageHeight as int) :
			uiArea.setMin(0, 0).setLengths(window.width, window.height)
	}

	@Override
	float getUiScale() {

		return window.uiScale
	}

	/**
	 * Render ImGui components within the context of an ImGui frame and optional
	 * dockspace.
	 */
	void useImGui(boolean createDockspace = false,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext') Closure closure) {

		imGuiGl3.newFrame()
		imGuiGlfw.newFrame()
		ImGui.newFrame()

		dockspaceId = createDockspace ? ImGui.dockSpaceOverViewport() : 0
		closure(this)

		ImGui.render()
		imGuiGl3.renderDrawData(ImGui.getDrawData())
	}
}
