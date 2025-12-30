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

import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.input.CursorPositionEvent

import imgui.ImGui
import imgui.type.ImBoolean
import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector3f
import static imgui.flag.ImGuiWindowFlags.*

/**
 * A configurable debug overlay starting with just an FPS counter.
 *
 * @author Emanuel Rabina
 */
class DebugOverlay extends ImGuiWindow {

	private final float updateRateSeconds
	private boolean cursorTracking = false
	private final Vector2f cursorPosition = new Vector2f()
	private final Vector3f worldPosition = new Vector3f()
	private long lastUpdateTimeMs = System.currentTimeMillis()
	private float updateTimer
	private float framerate
	private int width = 300

	/**
	 * Constructor, create a new FPS counter.
	 */
	DebugOverlay(float updateRateSeconds = 0f) {

		this.updateRateSeconds = updateRateSeconds
		updateTimer = updateRateSeconds // So we get a result the moment the counter is shown
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
		ImGui.pushFont(context.robotoMonoFont())

		ImGui.begin('Debug overlay', new ImBoolean(true), NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
		width = (int)ImGui.getWindowSizeX()
		ImGui.text("FPS: ${sprintf('%.1f', framerate)}, ${sprintf('%.1f', 1000 / framerate)}ms")
		if (cursorTracking) {
			ImGui.text("Cursor: ${sprintf('%.1f', cursorPosition.x)}, ${sprintf('%.1f', cursorPosition.y)}")
			ImGui.text("World: ${sprintf('%.1f', worldPosition.x)}, ${sprintf('%.1f', worldPosition.y)}")
		}
		ImGui.end()

		ImGui.popFont()
	}

	/**
	 * Include cursor position debugging in the overlay.
	 */
	DebugOverlay withCursorTracking(Camera camera, Matrix4fc cameraTransform, Window window) {

		window.on(CursorPositionEvent) { event ->
			cursorPosition.set(event.xPos(), event.yPos())
			camera.unproject(cursorPosition.x, cursorPosition.y, cameraTransform, worldPosition)
		}
		cursorTracking = true
		return this
	}
}
