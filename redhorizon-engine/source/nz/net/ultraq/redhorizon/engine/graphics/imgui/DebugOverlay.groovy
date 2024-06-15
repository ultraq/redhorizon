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

import nz.net.ultraq.redhorizon.engine.EngineStats
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiWindowFlags.*

/**
 * An overlay rendering pass for displaying debug information about the game.
 *
 * @author Emanuel Rabina
 */
class DebugOverlay implements ImGuiElement<DebugOverlay> {

	// Debug information
	private final Map<String, String> persistentLines = [:]
	private int debugWindowSizeX = 350
	private int debugWindowSizeY = 200
	private final EngineStats engineStats

	/**
	 * Constructor, create a new blank overlay.  This is made more useful by
	 * adding the renderers via the {@code add*} methods to get stats on their
	 * use.
	 */
	DebugOverlay(boolean enabled) {

		ImGuiLoggingAppender.instance?.on(ImGuiLogEvent) { event ->
			if (event.persistentKey) {
				persistentLines[event.persistentKey] = event.message
			}
		}
		engineStats = EngineStats.instance

		this.enabled = enabled
	}

	@Override
	void render(int dockspaceId, Framebuffer sceneFramebufferResult) {

		var viewport = ImGui.getMainViewport()
		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos(viewport.sizeX - debugWindowSizeX - 10 as float, viewport.workPosY + 10 as float)

		ImGui.begin('Debug overlay', new ImBoolean(true),
			NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
		debugWindowSizeX = ImGui.getWindowSizeX() as int
		debugWindowSizeY = ImGui.getWindowSizeY() as int

		ImGui.text("Framerate: ${sprintf('%.1f', ImGui.getIO().framerate)}fps, Frametime: ${sprintf('%.1f', 1000 / ImGui.getIO().framerate)}ms")
		ImGui.text("Draw calls: ${engineStats.drawCalls}")
		ImGui.text("Active meshes: ${engineStats.activeMeshes}")
		ImGui.text("Active textures: ${engineStats.activeTextures}")
		ImGui.text("Active framebuffers: ${engineStats.activeFramebuffers}")
		ImGui.text("Active uniform buffers: ${engineStats.activeUniformBuffers}")
		ImGui.text("Active sources: ${engineStats.activeSources}")
		ImGui.text("Active buffers: ${engineStats.activeBuffers}")
		engineStats.drawCalls.set(0)

		ImGui.separator()
		persistentLines.keySet().sort().each { key ->
			ImGui.text(persistentLines[key])
		}

		ImGui.end()
	}
}
