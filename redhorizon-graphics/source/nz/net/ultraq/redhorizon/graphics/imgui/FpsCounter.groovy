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


import imgui.ImFont
import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiWindowFlags.*

/**
 * A basic FPS counter overlay build using ImGui.
 *
 * @author Emanuel Rabina
 */
class FpsCounter {

	private final ImFont robotoMonoFont
	private final float updateRateSeconds
	private long lastUpdateTimeMs = System.currentTimeMillis()
	private float updateTimer
	private float framerate
	private int width = 300

	/**
	 * Constructor, create a new FPS counter tied to an existing window.
	 */
	FpsCounter(ImGuiContext imGuiContext, float updateRateSeconds) {

		robotoMonoFont = imGuiContext.robotoMonoFont
		this.updateRateSeconds = updateRateSeconds
		updateTimer = updateRateSeconds // So we get a result the moment the counter is shown
	}

	/**
	 * Draw the FPS counter in the upper right corner of the window.
	 */
	void render() {

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
		ImGui.pushFont(robotoMonoFont)

		ImGui.begin('Debug overlay', new ImBoolean(true), NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
		width = (int)ImGui.getWindowSizeX()
		ImGui.text("FPS: ${sprintf('%.1f', framerate)}, ${sprintf('%.1f', 1000 / framerate)}ms")
		ImGui.end()

		ImGui.popFont()
	}
}
