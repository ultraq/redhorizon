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

import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.audio.AudioRendererEvent
import nz.net.ultraq.redhorizon.engine.audio.BufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.audio.BufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.audio.SourceCreatedEvent
import nz.net.ultraq.redhorizon.engine.audio.SourceDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.OverlayRenderPass

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiWindowFlags.*

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * An overlay rendering pass for displaying debug information about the game.
 *
 * @author Emanuel Rabina
 */
class DebugOverlayRenderPass implements OverlayRenderPass {

	private static final int MAX_DEBUG_LINES = 10

	// Debug information
	private final BlockingQueue<String> debugLines = new ArrayBlockingQueue<>(MAX_DEBUG_LINES)
	private final Map<String, String> persistentLines = [:]
	private AtomicInteger drawCalls = new AtomicInteger()
	private AtomicInteger activeFramebuffers = new AtomicInteger()
	private AtomicInteger activeMeshes = new AtomicInteger()
	private AtomicInteger activeTextures = new AtomicInteger()
	private AtomicInteger activeSources = new AtomicInteger()
	private AtomicInteger activeBuffers = new AtomicInteger()
	private int debugWindowSizeX = 350
	private int debugWindowSizeY = 200

	/**
	 * Constructor, create a new blank overlay.  This is made more useful by
	 * adding the renderers via the {@code add*} methods to get stats on their
	 * use.
	 */
	DebugOverlayRenderPass(boolean enabled) {

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
	}

	/**
	 * Add the audio renderer to get stats on audio sources, buffers, etc.
	 */
	DebugOverlayRenderPass addAudioRenderer(AudioRenderer audioRenderer) {

		audioRenderer.on(AudioRendererEvent) { event ->
			switch (event) {
				case BufferCreatedEvent -> activeBuffers.incrementAndGet()
				case BufferDeletedEvent -> activeBuffers.decrementAndGet()
				case SourceCreatedEvent -> activeSources.incrementAndGet()
				case SourceDeletedEvent -> activeSources.decrementAndGet()
			}
		}
		return this
	}

	/**
	 * Add the graphics renderer to get status on draws, textures, etc.
	 */
	DebugOverlayRenderPass addGraphicsRenderer(GraphicsRenderer graphicsRenderer) {

		graphicsRenderer.on(GraphicsRendererEvent) { event ->
			switch (event) {
				case DrawEvent -> drawCalls.incrementAndGet()
				case FramebufferCreatedEvent -> activeFramebuffers.incrementAndGet()
				case FramebufferDeletedEvent -> activeFramebuffers.decrementAndGet()
				case MeshCreatedEvent -> activeMeshes.incrementAndGet()
				case MeshDeletedEvent -> activeMeshes.decrementAndGet()
				case TextureCreatedEvent -> activeTextures.incrementAndGet()
				case TextureDeletedEvent -> activeTextures.decrementAndGet()
			}
		}
		return this
	}

	@Override
	void render(GraphicsRenderer renderer, Framebuffer sceneFramebufferResult) {

		def viewport = ImGui.getMainViewport()
		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos(viewport.sizeX - debugWindowSizeX - 10 as float, viewport.workPosY + 10 as float)

		ImGui.begin('Debug overlay', new ImBoolean(true),
			NoNav | NoDecoration | NoSavedSettings | NoFocusOnAppearing | NoDocking | AlwaysAutoResize)
		debugWindowSizeX = ImGui.getWindowSizeX() as int
		debugWindowSizeY = ImGui.getWindowSizeY() as int

		ImGui.text("Framerate: ${sprintf('%.1f', ImGui.getIO().framerate)}fps, Frametime: ${sprintf('%.1f', 1000 / ImGui.getIO().framerate)}ms")
		ImGui.text("Draw calls: ${drawCalls}")
		ImGui.text("Active meshes: ${activeMeshes}")
		ImGui.text("Active textures: ${activeTextures}")
		ImGui.text("Active framebuffers: ${activeFramebuffers}")
		ImGui.text("Active sources: ${activeSources}")
		ImGui.text("Active buffers: ${activeBuffers}")
		drawCalls.set(0)

		ImGui.separator()
		persistentLines.keySet().sort().each { key ->
			ImGui.text(persistentLines[key])
		}

		if (debugLines.size()) {
			ImGui.separator()
			debugLines.each { line ->
				ImGui.textWrapped(line)
			}
		}

		ImGui.end()
	}
}
