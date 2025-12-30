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

package nz.net.ultraq.redhorizon.engine.extensions

import nz.net.ultraq.redhorizon.engine.input.EscapeToCloseBinding
import nz.net.ultraq.redhorizon.engine.input.ImGuiDebugOverlayBinding
import nz.net.ultraq.redhorizon.engine.input.ImGuiDebugWindowsBinding
import nz.net.ultraq.redhorizon.engine.input.VsyncBinding
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler

/**
 * Extensions for adding some common key bindings to the
 * {@link InputEventHandler}.
 *
 * @author Emanuel Rabina
 */
class InputEventHandlerExtensions {

	/**
	 * Add pressing {@code ESC} to close the window.
	 */
	static InputEventHandler addEscapeToCloseBinding(InputEventHandler self, Window window) {

		return self.addInputBinding(new EscapeToCloseBinding(window))
	}

	/**
	 * Add pressing {@code I} to toggle ImGui debug windows, and {@code O} to
	 * toggle ImGui debug overlays.
	 */
	static InputEventHandler addImGuiDebugBindings(InputEventHandler self, List<ImGuiWindow> overlays,
		List<ImGuiWindow> windows) {

		return self
			.addInputBinding(new ImGuiDebugOverlayBinding(overlays))
			.addInputBinding(new ImGuiDebugWindowsBinding(windows))
	}

	/**
	 * Add pressing {@code V} to toggle/cycle vsync options.
	 */
	static InputEventHandler addVSyncBinding(InputEventHandler self, Window window) {

		return self.addInputBinding(new VsyncBinding(window))
	}
}
