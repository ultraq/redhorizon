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

import groovy.transform.PackageScope
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Wrapper around all of the `imgui-java` binding classes, hiding all of the
 * setup needed to make it work.
 * 
 * @author Emanuel Rabina
 */
@Singleton(strict = false)
class ImGuiRenderer implements AutoCloseable {

	static final int MAX_DEBUG_LINES = 10
	private static ImGuiRenderer rendererInstance

	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw
	private final BlockingQueue<String> debugLines = new ArrayBlockingQueue<>(MAX_DEBUG_LINES)

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

		rendererInstance = this
	}

	/**
	 * Add a line to be displayed in the debug overlay.  A maximum of
	 * {@link ImGuiRenderer#MAX_DEBUG_LINES} are allowed in the overlay, with old
	 * lines being pushed out.
	 * 
	 * @param line
	 */
	@PackageScope
	void addDebugLine(String line) {

		while (!debugLines.offer(line)) {
			debugLines.poll()
		}
	}

	@Override
	void close() {

		imGuiGlfw.dispose()
		imGuiGl3.dispose()
		ImGui.destroyContext()
	}

	/**
	 * Draws a small overlay containing the current framerate, frametime, and any
	 * recent log messages.
	 */
	void drawDebugOverlay() {

		ImGui.setNextWindowPos(10, 10)
		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.begin('Debug overlay', new ImBoolean(true),
			ImGuiWindowFlags.NoNav | ImGuiWindowFlags.NoDecoration |  ImGuiWindowFlags.AlwaysAutoResize |
				ImGuiWindowFlags.NoSavedSettings | ImGuiWindowFlags.NoFocusOnAppearing | ImGuiWindowFlags.NoMove)
		ImGui.text("Framerate: ${sprintf('%.1f', ImGui.getIO().framerate)}fps, Frametime: ${sprintf('%.1f', 1000 / ImGui.getIO().framerate)}ms")
		if (debugLines.size()) {
			ImGui.separator()
			debugLines.toArray().each { line ->
				ImGui.text(line)
			}
		}
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
	 * Return the running instance of the ImGui renderer.
	 * 
	 * @return
	 */
	static ImGuiRenderer getInstance() {

		return rendererInstance
	}

	/**
	 * Mark the beginning of the rendering loop.
	 */
	void startFrame() {

		imGuiGlfw.newFrame()
		ImGui.newFrame()
	}
}
