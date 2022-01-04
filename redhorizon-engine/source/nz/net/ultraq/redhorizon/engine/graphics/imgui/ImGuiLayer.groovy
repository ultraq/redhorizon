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
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsContext
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.RendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent
import nz.net.ultraq.redhorizon.engine.input.InputSource
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.geometry.Dimension
import static nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent.*

import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.*
import static imgui.flag.ImGuiConfigFlags.*
import static imgui.flag.ImGuiDockNodeFlags.*
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.*
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Wrapper around all of the `imgui-java` binding classes, hiding all of the
 * setup needed to make it work.
 * 
 * @author Emanuel Rabina
 */
class ImGuiLayer implements AutoCloseable, InputSource {

	private static final int MAX_DEBUG_LINES = 10

	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw
	private boolean drawChrome = true

	// Debug overlay
	private final BlockingQueue<String> debugLines = new ArrayBlockingQueue<>(MAX_DEBUG_LINES)
	private final Map<String,String> persistentLines = [:]
	private int drawCalls = 0
	private int activeFramebuffers = 0
	private int activeMeshes = 0
	private int activeTextures = 0
	private int debugWindowSizeX = 350
	private int debugWindowSizeY = 200

	// Options
	private boolean debugOverlay
	private boolean shaderScanlines
	private boolean shaderSharpUpscaling

	/**
	 * Create a new ImGui renderer to work with an existing OpenGL window and
	 * renderer.
	 * 
	 * @param config
	 * @param context
	 * @param renderer
	 */
	ImGuiLayer(GraphicsConfiguration config, GraphicsContext context, EventTarget renderer) {

		debugOverlay = config.debug
		shaderScanlines = config.scanlines
		shaderSharpUpscaling = true

		// TODO: Split the ImGui setup from the debug overlay
		imGuiGlfw = new ImGuiImplGlfw()
		imGuiGl3 = new ImGuiImplGl3()
		ImGui.createContext()

		def io = ImGui.getIO()
		io.setConfigFlags(DockingEnable)
		io.fonts.addFontFromFileTTF('Roboto-Medium.ttf', 16)

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

		context.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_O) {
					drawChrome = !drawChrome
				}
				else if (event.key == GLFW_KEY_D) {
					debugOverlay = !debugOverlay
				}
			}
		}
	}

	@Override
	void close() {

		imGuiGl3.dispose()
		imGuiGlfw.dispose()
		ImGui.destroyContext()
	}

	/**
	 * Draws a small overlay containing the current framerate, frametime, and any
	 * recent log messages.
	 */
	private void drawDebugOverlay() {

		def viewport = ImGui.getMainViewport()
		ImGui.setNextWindowBgAlpha(0.4f)
		ImGui.setNextWindowPos(viewport.sizeX - debugWindowSizeX - 10 as float, drawChrome ? 55 : 10)

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

	/**
	 * Draw the scene from the given framebuffer into an ImGui window that will
	 * take up the whole screen by default.
	 * 
	 * @param sceneFramebufferResult
	 */
	private void drawScene(Framebuffer sceneFramebufferResult) {

		ImGui.setNextWindowPos(100, 100, FirstUseEver)
		ImGui.setNextWindowSize(640, 400, FirstUseEver)
		ImGui.pushStyleVar(WindowPadding, 0, 0)

		ImGui.begin("Scene", new ImBoolean(true), NoScrollbar)
		ImGui.popStyleVar()

		def framebufferSize = sceneFramebufferResult.texture.size
		def windowSize = new Dimension(ImGui.getContentRegionAvailX() as int, ImGui.getContentRegionAvailY() as int)
		def imageSizeX = windowSize.width
		def imageSizeY = windowSize.height
		def uvX = 0f
		def uvY = 0f
		def cursorX = 0
		def cursorY = ImGui.getCursorPosY()

		// Window is wider
		if (windowSize.aspectRatio > framebufferSize.aspectRatio) {
			uvX = 1 / (framebufferSize.width - (framebufferSize.width - windowSize.width)) as float
			imageSizeX = imageSizeY * framebufferSize.aspectRatio as float
			cursorX = (windowSize.width - imageSizeX) * 0.5f as float
		}
		// Window is taller
		else if (windowSize.aspectRatio < framebufferSize.aspectRatio) {
			uvY = 1 / (framebufferSize.height - (framebufferSize.height - windowSize.height)) as float
			imageSizeY = imageSizeX / framebufferSize.aspectRatio as float
			cursorY = cursorY +(windowSize.height - imageSizeY) * 0.5f as float
		}

		ImGui.setCursorPos(cursorX, cursorY)
		ImGui.image(sceneFramebufferResult.texture.textureId, imageSizeX, imageSizeY,
			uvX, 1 - uvY as float, 1 - uvX as float, uvY)

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
	 * 
	 * @param sceneFramebufferResult
	 */
	void render(Framebuffer sceneFramebufferResult) {

		if (drawChrome) {
			setUpDockspace()
			drawScene(sceneFramebufferResult)
		}

		if (debugOverlay) {
			drawDebugOverlay()
		}
	}

	/**
	 * Build the docking window into which the app will be rendered.
	 */
	private void setUpDockspace() {

		def viewport = ImGui.getMainViewport()
		ImGui.setNextWindowPos(0, 0)
		ImGui.setNextWindowSize(viewport.workSizeX, viewport.workSizeY + 2 as float)
		ImGui.pushStyleVar(WindowBorderSize, 0)
		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.pushStyleVar(WindowRounding, 0)

		ImGui.begin('Dockspace', new ImBoolean(true),
			NoTitleBar | NoCollapse | NoResize | NoMove | NoBringToFrontOnFocus | NoNavFocus | MenuBar | NoDocking | NoBackground)
		ImGui.popStyleVar(3)

		ImGui.dockSpace(ImGui.getID('MyDockspace'), viewport.workSizeX, viewport.workSizeY - 20 as float, PassthruCentralNode)

		if (ImGui.beginMenuBar()) {

			if (ImGui.beginMenu('File')) {
				if (ImGui.menuItem('Exit')) {
					trigger(new GuiEvent(EVENT_TYPE_STOP))
				}
				ImGui.endMenu()
			}

			if (ImGui.beginMenu('Options')) {
				if (ImGui.menuItem('Debug overlay', null, debugOverlay)) {
					debugOverlay = !debugOverlay
				}
				if (ImGui.menuItem('Scanlines', null, shaderScanlines)) {
					shaderScanlines = !shaderScanlines
					trigger(new ChangeEvent('Scanlines', shaderScanlines))
				}
				if (ImGui.menuItem('Sharp upscaling', null, shaderSharpUpscaling)) {
					shaderSharpUpscaling = !shaderSharpUpscaling
					trigger(new ChangeEvent('SharpUpscaling', shaderSharpUpscaling))
				}
				ImGui.endMenu()
			}

			ImGui.endMenuBar()
		}

		ImGui.end()
	}

	/**
	 * Mark the beginning of the rendering loop.
	 */
	void startFrame() {

		imGuiGlfw.newFrame()
		ImGui.newFrame()
	}
}
