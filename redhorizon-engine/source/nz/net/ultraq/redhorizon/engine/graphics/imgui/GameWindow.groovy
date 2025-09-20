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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture

import imgui.ImGui
import imgui.type.ImBoolean
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static imgui.flag.ImGuiCond.*
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.*

import groovy.transform.TupleConstructor

/**
 * When window chrome is enabled, the scene will be rendered to an ImGui image
 * texture held by this class.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor
class GameWindow implements ImGuiChrome, EventTarget<GameWindow> {

	private static final Logger logger = LoggerFactory.getLogger(GameWindow)

	final float targetAspectRatio

	private boolean focused
	private boolean hovered
	private Dimension lastWindowSize

	@Override
	boolean isFocused() {

		return focused
	}

	@Override
	boolean isHovered() {

		return hovered
	}

	@Override
	void render(int dockspaceId, Framebuffer sceneFramebufferResult) {

		ImGui.setNextWindowSize(800, 600, FirstUseEver)
		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.begin('Game', new ImBoolean(true), NoCollapse | NoScrollbar | NoScrollWithMouse)
		ImGui.popStyleVar()

		focused = ImGui.isWindowFocused()
		hovered = ImGui.isWindowHovered()

		imgui.internal.ImGui.dockBuilderDockWindow('Game', dockspaceId)
		imgui.internal.ImGui.dockBuilderFinish(dockspaceId)

		var framebufferSize = sceneFramebufferResult.texture.size
		var windowSize = new Dimension(ImGui.contentRegionMaxX as int, ImGui.contentRegionMaxY as int)
		var imageSizeX = windowSize.width()
		var imageSizeY = windowSize.height()
		var uvX = 0f
		var uvY = 0f
		var cursorX = 0
		var cursorY = ImGui.getCursorPosY()

		// Window is wider
		if (windowSize.aspectRatio > framebufferSize.aspectRatio) {
			uvX = 1 / (framebufferSize.width() - (framebufferSize.width() - windowSize.width())) as float
			imageSizeX = imageSizeY * framebufferSize.aspectRatio as float
			cursorX = (windowSize.width() - imageSizeX) * 0.5f as float
		}
		// Window is taller
		else if (windowSize.aspectRatio < framebufferSize.aspectRatio) {
			uvY = 1 / (framebufferSize.height() - (framebufferSize.height() - windowSize.height())) as float
			imageSizeY = imageSizeX / framebufferSize.aspectRatio as float
			cursorY = cursorY + (windowSize.height() - imageSizeY) * 0.5f as float
		}

		ImGui.setCursorPos(cursorX, cursorY)
		ImGui.image(((OpenGLTexture)sceneFramebufferResult.texture).textureId, imageSizeX, imageSizeY,
			uvX, 1 - uvY as float, 1 - uvX as float, uvY)

		ImGui.end()

		if (windowSize != lastWindowSize) {
			logger.debug('Scene window changed to {}', windowSize)
			var targetResolution = windowSize.calculateFit(targetAspectRatio)
			logger.debug('Target resolution changed to {}', targetResolution)
			trigger(new FramebufferSizeEvent(windowSize, windowSize, targetResolution))
			lastWindowSize = windowSize
		}
	}
}
