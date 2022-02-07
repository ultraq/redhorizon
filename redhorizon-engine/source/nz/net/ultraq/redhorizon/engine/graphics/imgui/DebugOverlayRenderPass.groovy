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

import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.OverlayRenderPass
import nz.net.ultraq.redhorizon.engine.graphics.RendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiWindowFlags.*

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * An overlay rendering pass for displaying debug information about the game.
 * 
 * @author Emanuel Rabina
 */
class DebugOverlayRenderPass implements OverlayRenderPass {

	private static final int MAX_DEBUG_LINES = 10

	boolean enabled
	final ImGuiLayer imGuiLayer

	// Debug information
	private final BlockingQueue<String> debugLines = new ArrayBlockingQueue<>(MAX_DEBUG_LINES)
	private final Map<String,String> persistentLines = [:]
	private int drawCalls = 0
	private int activeFramebuffers = 0
	private int activeMeshes = 0
	private int activeTextures = 0
	private int debugWindowSizeX = 350
	private int debugWindowSizeY = 200

	/**
	 * Constructor, tie the debug overlay to the renderer so it can get the
	 * information it needs to build the display.
	 * 
	 * @param renderer
	 * @param imGuiLayer
	 * @param enabled
	 */
	DebugOverlayRenderPass(GraphicsRenderer renderer, ImGuiLayer imGuiLayer, boolean enabled) {

		renderer.on(RendererEvent) { event ->
			if (event instanceof DrawEvent) {
				drawCalls++
			}
			else if (event instanceof FramebufferCreatedEvent) {
				activeFramebuffers++
			}
			else if (event instanceof FramebufferDeletedEvent) {
				activeFramebuffers--
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

		ImGuiLoggingAppender.instance.on(ImGuiLogEvent) { event ->
			if (event.persistentKey) {
				persistentLines[event.persistentKey] = event.message
			}
			else {
				while (!debugLines.offer(event.message)) {
					debugLines.poll()
				}
			}
		}

		this.enabled = enabled
		this.imGuiLayer = imGuiLayer
	}

	@Override
	void render(GraphicsRenderer renderer, Framebuffer sceneFramebufferResult) {

		def viewport = ImGui.getMainViewport()
		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos(viewport.sizeX - debugWindowSizeX - 10 as float, 10)

		ImGui.begin('Debug overlay', new ImBoolean(true),
			NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
		debugWindowSizeX = ImGui.getWindowSizeX() as int
		debugWindowSizeY = ImGui.getWindowSizeY() as int

		ImGui.text("Framerate: ${sprintf('%.1f', ImGui.getIO().framerate)}fps, Frametime: ${sprintf('%.1f', 1000 / ImGui.getIO().framerate)}ms")
		ImGui.text("Draw calls: ${drawCalls}")
		ImGui.text("Active meshes: ${activeMeshes}")
		ImGui.text("Active textures: ${activeTextures}")
		ImGui.text("Active framebuffers: ${activeFramebuffers}")
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
}
