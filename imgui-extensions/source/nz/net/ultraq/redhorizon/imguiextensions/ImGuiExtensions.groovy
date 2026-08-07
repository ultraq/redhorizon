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

package nz.net.ultraq.redhorizon.imguiextensions

import imgui.ImFont
import imgui.ImGui
import imgui.type.ImBoolean

/**
 * Extensions for the ImGui library.
 *
 * @author Emanuel Rabina
 */
class ImGuiExtensions {

	/**
	 * Convenience method to use a font within the context of the closure by
	 * wrapping it with the necessary {@link ImGui#pushFont(ImFont, float)} and
	 * {@link ImGui#popFont()} calls.
	 */
	static void useFont(ImGui self, ImFont font, float size, Closure closure) {

		ImGui.pushFont(font, size)
		closure()
		ImGui.popFont()
	}

	/**
	 * Convenience method to use an ID within the context of the closure by
	 * wrapping it with the necessary {@link ImGui#pushID(String)} and
	 * {@link ImGui#popID()} calls.
	 */
	static void useId(ImGui self, String id, Closure closure) {

		ImGui.pushID(id)
		closure()
		ImGui.popID()
	}

	/**
	 * Convenience method to create a window using {@link ImGui#begin(String, ImBoolean, int)},
	 * call the closure, then finish window creation with {@link ImGui#end()}.
	 */
	static void useWindow(ImGui self, String title, ImBoolean open, int flags = 0, Closure closure) {

		ImGui.begin(title, open, flags)
		closure()
		ImGui.end()
	}
}
