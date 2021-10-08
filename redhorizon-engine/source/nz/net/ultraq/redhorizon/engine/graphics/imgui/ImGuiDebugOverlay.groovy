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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsContext
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.RendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent
import nz.net.ultraq.redhorizon.events.Event
import nz.net.ultraq.redhorizon.events.EventTarget

import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImBoolean
import static imgui.flag.ImGuiWindowFlags.*

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Wrapper around all of the `imgui-java` binding classes, hiding all of the
 * setup needed to make it work.
 * 
 * @author Emanuel Rabina
 */
class ImGuiDebugOverlay implements AutoCloseable, EventTarget {

	private static final int MAX_DEBUG_LINES = 10

	private final GraphicsContext context
	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw
	private final ExecutorService executorService = Executors.newCachedThreadPool()

	// Debug overlay
	private final BlockingQueue<String> debugLines = new ArrayBlockingQueue<>(MAX_DEBUG_LINES)
	private final Map<String,String> persistentLines = [:]
	private int drawCalls = 0
	private int activeFramebuffers = 0
	private int activeMeshes = 0
	private int activeTextures = 0

	// Options overlay
	private ImBoolean shaderScanlines
	private boolean lastShaderScanlinesState
	private ImBoolean shaderSharpUpscaling
	private boolean lastShaderSharpUpscalingState

	/**
	 * Create a new ImGui renderer to work with an existing OpenGL window and
	 * renderer.
	 * 
	 * @param config
	 * @param context
	 * @param renderer
	 */
	ImGuiDebugOverlay(GraphicsConfiguration config, GraphicsContext context, EventTarget renderer) {

		this.context = context

		shaderScanlines = new ImBoolean(config.scanlines)
		lastShaderScanlinesState = shaderScanlines.get()
		shaderSharpUpscaling = new ImBoolean(true)
		lastShaderSharpUpscalingState = shaderSharpUpscaling.get()

		imGuiGlfw = new ImGuiImplGlfw()
		imGuiGl3 = new ImGuiImplGl3()
		ImGui.createContext()
		imGuiGlfw.init(context.window, true)
		imGuiGl3.init('#version 410 core')

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

		ImGuiDebugOverlayAppender.instance.on(ImGuiLogEvent) { event ->
			if (event.persistentKey) {
				persistentLines[event.persistentKey] = event.message
			}
			else {
				while (!debugLines.offer(event.message)) {
					debugLines.poll()
				}
			}
		}
	}

	@Override
	void close() {

		executorService.shutdownAwaitTermination()
		imGuiGl3.dispose()
		imGuiGlfw.dispose()
		ImGui.destroyContext()
	}

	/**
	 * Draws a small overlay containing the current framerate, frametime, and any
	 * recent log messages.
	 */
	private void drawDebugOverlay() {

		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos(10, 10)

		ImGui.begin('Debug overlay', new ImBoolean(true),
			NoNav | NoDecoration | AlwaysAutoResize | NoSavedSettings | NoFocusOnAppearing | NoMove)

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

	/**
	 * Draw a small overlay of options that can be modified while running.
	 */
	private void drawGameOptions() {

		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos(context.windowSize.width - 210, 10)
		ImGui.setNextWindowSize(200, 100)

		ImGui.begin('Options', new ImBoolean(true),
			NoNav | NoDecoration | NoResize | NoSavedSettings | NoFocusOnAppearing | NoMove)

		ImGui.text('Post-processing effects')
		ImGui.separator()
		ImGui.checkbox('Scanlines', shaderScanlines)
		ImGui.checkbox('Sharp upscaling', shaderSharpUpscaling)

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
	 * Draw all of the ImGui elements to the screen.
	 */
	void render() {

		drawDebugOverlay()
		drawGameOptions()
	}

	/**
	 * Mark the beginning of the rendering loop.
	 */
	void startFrame() {

		imGuiGlfw.newFrame()
		ImGui.newFrame()

		def currentShaderScanlinesState = shaderScanlines.get()
		if (currentShaderScanlinesState != lastShaderScanlinesState) {
			triggerOnSeparateThread(new ChangeEvent('Scanlines', currentShaderScanlinesState))
			lastShaderScanlinesState = currentShaderScanlinesState
		}

		def currentShaderSharpUpscalingState = shaderSharpUpscaling.get()
		if (currentShaderSharpUpscalingState != lastShaderSharpUpscalingState) {
			triggerOnSeparateThread(new ChangeEvent('SharpUpscaling', currentShaderSharpUpscalingState))
			lastShaderSharpUpscalingState = currentShaderSharpUpscalingState
		}
	}

	/**
	 * Fire an event on a separate thread using the built-in executor.
	 * 
	 * @param event
	 */
	private void triggerOnSeparateThread(Event event) {

		executorService.execute { ->
			trigger(event)
		}
	}
}
