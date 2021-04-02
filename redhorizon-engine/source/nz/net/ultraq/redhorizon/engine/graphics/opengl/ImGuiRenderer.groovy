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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.RendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent

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
class ImGuiRenderer implements AutoCloseable {

	static final int MAX_DEBUG_LINES = 10
	private static ImGuiRenderer rendererInstance

	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw
	private final BlockingQueue<String> debugLines = new ArrayBlockingQueue<>(MAX_DEBUG_LINES)
	private final Map<String,String> persistentLines = [:]
	private int drawCalls = 0
	private int activeMeshes = 0
	private int activeTextures = 0

	/**
	 * Create a new ImGui renderer to work with an existing OpenGL window and
	 * renderer.
	 * 
	 * @param context
	 * @param renderer
	 */
	ImGuiRenderer(OpenGLContext context, OpenGLRenderer renderer) {

		ImGui.createContext()
		imGuiGl3 = new ImGuiImplGl3()
		imGuiGlfw = new ImGuiImplGlfw()
		imGuiGl3.init('#version 330 core')
		imGuiGlfw.init(context.window, true)

		renderer.on(RendererEvent) { event ->
			if (event instanceof DrawEvent) {
				drawCalls++
			}
			else if (event instanceof MeshCreatedEvent) {
				activeMeshes++
			}
			else if (event instanceof MeshDeletedEvent) {
				activeMeshes--
			}
			else if (event instanceof TextureCreatedEvent) {
				activeTextures++
			}
			else if (event instanceof TextureDeletedEvent) {
				activeTextures--
			}
		}

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
		ImGui.text("Draw calls: ${drawCalls}")
		ImGui.text("Active meshes: ${activeMeshes}")
		ImGui.text("Active textures: ${activeTextures}")
		drawCalls = 0

		ImGui.separator()
		persistentLines.keySet().sort().each { key ->
			ImGui.text(persistentLines[key])
		}

		if (debugLines.size()) {
			ImGui.separator()
			debugLines.each { line ->
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
	 * Set a line that appears with the other persistent debug lines in the
	 * overlay.
	 * 
	 * @param key
	 * @param line
	 */
	@PackageScope
	void setPersistentLine(String key, String line) {

		persistentLines[key] = line
	}

	/**
	 * Mark the beginning of the rendering loop.
	 */
	void startFrame() {

		imGuiGlfw.newFrame()
		ImGui.newFrame()
	}
}
