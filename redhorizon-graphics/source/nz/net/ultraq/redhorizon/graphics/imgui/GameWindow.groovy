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

import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture

import imgui.ImGui
import imgui.ImGuiWindowClass
import imgui.ImVec2
import imgui.flag.ImGuiDockNodeFlags
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCol.WindowBg
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.NoDecoration

/**
 * An ImGui window into which a single Image element is drawn, which will be the
 * result of rendering the scene.  This is so the game can participate in
 * docking.
 *
 * @author Emanuel Rabina
 */
class GameWindow {

	private static final ImBoolean imBooleanTrue = new ImBoolean(true)
	private static final ImGuiWindowClass gameWindowClass = new ImGuiWindowClass().tap {
		dockNodeFlagsOverrideSet = ImGuiDockNodeFlags.AutoHideTabBar
	}

	float lastImageX = 0f
	float lastImageY = 0f
	float lastImageWidth = 0f
	float lastImageHeight = 0f
	private ImVec2 windowPos = new ImVec2()
	private Colour backgroundColour = Colour.BLACK

	/**
	 * Draw the game window into which the scene will be rendered.
	 */
	void render(int dockspaceId, Framebuffer framebuffer) {

		imgui.internal.ImGui.dockBuilderDockWindow('Game', dockspaceId)

		ImGui.setNextWindowClass(gameWindowClass)
		ImGui.pushStyleVar(WindowBorderSize, 0f)
		ImGui.pushStyleVar(WindowPadding, 0f, 0f)
		ImGui.pushStyleVar(WindowRounding, 0f)
		ImGui.pushStyleColor(WindowBg, backgroundColour.r, backgroundColour.g, backgroundColour.b, backgroundColour.a)
		ImGui.begin('Game', imBooleanTrue, NoDecoration)

		ImGui.getWindowPos(windowPos)
		var windowWidth = ImGui.getContentRegionAvailX()
		var windowHeight = ImGui.getContentRegionAvailY()
		var windowAspectRatio = windowWidth / windowHeight
		var framebufferAspectRatio = framebuffer.width / framebuffer.height
		var imageSizeX = windowWidth
		var imageSizeY = windowHeight
		var uvX = 0f
		var uvY = 0f
		var cursorX = 0
		var cursorY = ImGui.cursorPosY

		// Window is wider
		if (windowAspectRatio > framebufferAspectRatio) {
			uvX = 1 / (framebuffer.width - (framebuffer.height - windowWidth)) as float
			imageSizeX = windowHeight * framebufferAspectRatio as float
			cursorX = (windowWidth - imageSizeX) * 0.5f as float
		}
		// Window is taller
		else if (windowAspectRatio < framebufferAspectRatio) {
			uvY = 1 / (framebuffer.width - (framebuffer.height - windowHeight)) as float
			imageSizeY = windowWidth / framebufferAspectRatio as float
			cursorY = cursorY + (windowHeight - imageSizeY) * 0.5f as float
		}

		ImGui.setCursorPos(cursorX, cursorY)
		ImGui.image(((OpenGLTexture)framebuffer.texture).textureId, imageSizeX, imageSizeY,
			uvX, 1 - uvY as float, 1 - uvX as float, uvY)

		ImGui.popStyleColor()
		ImGui.popStyleVar(3)
		ImGui.end()

		lastImageX = cursorX + windowPos.x as float
		lastImageY = cursorY + windowPos.y as float
		lastImageWidth = imageSizeX
		lastImageHeight = imageSizeY
	}

	/**
	 * Adjust the background colour used for the game window.  Usually kept in
	 * sync with the GL clear colour.
	 */
	void setBackgroundColour(Colour colour) {

		backgroundColour = colour
	}
}
