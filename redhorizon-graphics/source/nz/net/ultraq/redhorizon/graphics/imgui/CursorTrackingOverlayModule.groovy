/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * A module for tracking the cursor position in the debug overlay.
 *
 * @author Emanuel Rabina
 */
class CursorTrackingOverlayModule implements DebugOverlayModule {

	private final Vector2f cursorPosition = new Vector2f()
	private final Vector3f worldPosition = new Vector3f()

	/**
	 * Constructor, read cursor position updates from the window to log out later.
	 */
	CursorTrackingOverlayModule(Window window, Camera camera, Matrix4fc cameraTransform) {

		window.on(CursorPositionEvent) { event ->
			cursorPosition.set(event.xPos(), event.yPos())
			camera.unproject(cursorPosition.x, cursorPosition.y, cameraTransform, worldPosition)
		}
	}

	@Override
	void render() {

		ImGui.text("Cursor: ${sprintf('%.1f', cursorPosition.x)}, ${sprintf('%.1f', cursorPosition.y)}")
		ImGui.text("World: ${sprintf('%.1f', worldPosition.x)}, ${sprintf('%.1f', worldPosition.y)}")
	}
}
