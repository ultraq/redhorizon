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

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiWindowFlags.*

/**
 * A configurable debug overlay starting with just an FPS counter.
 *
 * @author Emanuel Rabina
 */
class DebugOverlay implements ImGuiModule {

	private final float updateRateSeconds
	private long lastUpdateTimeMs = System.currentTimeMillis()
	private float updateTimer
	private float framerate
	private int width = 300
	private final List<DebugOverlayModule> modules = []

	/**
	 * Constructor, create a new FPS counter.
	 */
	DebugOverlay(float updateRateSeconds = 0f) {

		this.updateRateSeconds = updateRateSeconds
		updateTimer = updateRateSeconds // So we get a result the moment the counter is shown
	}

	/**
	 * Add a module to include with the debug overlay.
	 */
	DebugOverlay addModule(DebugOverlayModule module) {

		modules << module
		return this
	}

	/**
	 * An overload of the {@code <<} operator as an alias to {@link #addModule(DebugOverlayModule)}.
	 */
	DebugOverlay leftShift(DebugOverlayModule module) {

		return addModule(module)
	}

	@Override
	void render(ImGuiContext context) {

		var currentTimeMs = System.currentTimeMillis()
		var delta = (currentTimeMs - lastUpdateTimeMs) / 1000 as float
		updateTimer += delta
		if (updateTimer > updateRateSeconds) {
			framerate = ImGui.getIO().framerate
			updateTimer -= updateRateSeconds
		}
		lastUpdateTimeMs = currentTimeMs

		var viewport = ImGui.getMainViewport()
		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos((float)(viewport.workPosX + viewport.sizeX - width), viewport.workPosY)
		ImGui.pushFont(context.monospaceFont)

		ImGui.begin('Debug overlay', new ImBoolean(true), NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
		width = (int)ImGui.getWindowSizeX()
		ImGui.text("FPS: ${sprintf('%.1f', framerate)}, ${sprintf('%.1f', 1000 / framerate)}ms")
		if (modules) {
			ImGui.separator()
			modules*.render()
		}
		ImGui.end()

		ImGui.popFont()
	}
}
