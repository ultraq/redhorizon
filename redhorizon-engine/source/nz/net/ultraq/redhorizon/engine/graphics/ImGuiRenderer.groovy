/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImBoolean

/**
 * Wrapper around all of the `imgui-java` binding classes, hiding all of the
 * setup needed to make it work.
 * 
 * @author Emanuel Rabina
 */
class ImGuiRenderer implements AutoCloseable {

	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw

	/**
	 * Create a new ImGui renderer to work with an existing OpenGL window.
	 * 
	 * @param context
	 */
	ImGuiRenderer(OpenGLContext context) {

		ImGui.createContext()
		imGuiGl3 = new ImGuiImplGl3()
		imGuiGlfw = new ImGuiImplGlfw()
		imGuiGl3.init('#version 330 core')
		imGuiGlfw.init(context.window, true)
	}

	@Override
	void close() {

		imGuiGlfw.dispose()
		imGuiGl3.dispose()
		ImGui.destroyContext()
	}

	/**
	 * Draws a small overlay containing the current framerate and frametime.
	 */
	void drawFpsOverlay() {

		// TODO: Change this to a generic drawOverlay() method that takes a list of
		//       lines to include
		ImGui.setNextWindowPos(10, 10)
		ImGui.setNextWindowBgAlpha(0.25f)
		ImGui.begin('Debug overlay', new ImBoolean(true),
			ImGuiWindowFlags.NoNav | ImGuiWindowFlags.NoDecoration |  ImGuiWindowFlags.AlwaysAutoResize |
				ImGuiWindowFlags.NoSavedSettings | ImGuiWindowFlags.NoFocusOnAppearing | ImGuiWindowFlags.NoMove)
		ImGui.text("Framerate: ${sprintf('%.1f', ImGui.getIO().framerate)}fps, Frametime: ${sprintf('%.1f', 1000 / ImGui.getIO().framerate)}ms")
		ImGui.end()
	}

	/**
	 * Mark the end of the rendering loop, at which point any ImGui elements will
	 * be drawn.
	 */
	void endFrame() {

		ImGui.render()
		imGuiGl3.renderDrawData(ImGui.getDrawData())
	}

	/**
	 * Mark the beginning of the rendering loop.
	 */
	void startFrame() {

		imGuiGlfw.newFrame()
		ImGui.newFrame()
	}
}
