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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLTexture
import nz.net.ultraq.redhorizon.engine.input.InputSource
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.events.EventTarget

import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImBoolean
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static imgui.flag.ImGuiCond.FirstUseEver
import static imgui.flag.ImGuiConfigFlags.DockingEnable
import static imgui.flag.ImGuiDockNodeFlags.NoResize
import static imgui.flag.ImGuiDockNodeFlags.PassthruCentralNode
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.*
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor

/**
 * Wrapper around all of the `imgui-java` binding classes, hiding all of the
 * setup needed to make it work.
 *
 * @author Emanuel Rabina
 */
class ImGuiLayer implements AutoCloseable, InputSource {

	private static final Logger logger = LoggerFactory.getLogger(ImGuiLayer)

	static ImFont robotoFont
	static ImFont robotoMonoFont

	final GameWindow gameWindow

	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw
	private final List<ImGuiChrome> chrome = []
	private final List<ImGuiOverlay> overlays = []

	private boolean drawUiElements
	private boolean drawOverlays
	private boolean shaderScanlines
	private boolean shaderSharpUpscaling

	/**
	 * Create a new ImGui renderer to work with an existing OpenGL window and
	 * renderer.
	 */
	ImGuiLayer(GraphicsConfiguration config, Window window) {

		drawUiElements = config.startWithChrome
		drawOverlays = config.debug
		shaderScanlines = config.scanlines
		shaderSharpUpscaling = true

		imGuiGlfw = new ImGuiImplGlfw()
		imGuiGl3 = new ImGuiImplGl3()
		ImGui.createContext()

		var io = ImGui.getIO()
		io.setConfigFlags(DockingEnable)

		var fontConfig1 = new ImFontConfig()
		robotoFont = getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/imgui/Roboto-Medium.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, 16 * window.monitorScale as float, fontConfig1)
		}
		fontConfig1.destroy()
		io.setFontDefault(robotoFont)

		var fontConfig2 = new ImFontConfig()
		robotoMonoFont = getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/imgui/RobotoMono-Medium.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, 16 * window.monitorScale as float, fontConfig2)
		}
		fontConfig2.destroy()

		imGuiGlfw.init(window.handle, true)
		imGuiGl3.init('#version 410 core')

		gameWindow = new GameWindow(config.targetAspectRatio)
		addChrome(gameWindow)

		window.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				switch (event.key) {
					case GLFW_KEY_I -> drawUiElements = !drawUiElements
					case GLFW_KEY_O -> drawOverlays = !drawOverlays
				}
			}
		}
	}

	/**
	 * Register any ImGui chrome to be drawn.
	 */
	void addChrome(ImGuiChrome uiChrome) {

		chrome << uiChrome
	}

	/**
	 * Register any ImGui overlay to be drawn.
	 */
	void addOverlay(ImGuiOverlay overlay) {

		overlays << overlay
	}

	@Override
	void close() {

		imGuiGl3.shutdown()
		imGuiGlfw.shutdown()
		ImGui.destroyContext()
	}

	/**
	 * Find the ImGui chrome item by class.
	 */
	<T extends ImGuiChrome> T findChrome(Class<T> clazz) {

		return (T)chrome.find { c -> c.class == clazz }
	}

	/**
	 * Find the ImGui overlay item by class.
	 */
	<T extends ImGuiOverlay> T findOverlay(Class<T> clazz) {

		return (T)overlays.find { overlay -> overlay.class == clazz }
	}

	/**
	 * Automatically mark the beginning and end of a frame as before and after the
	 * execution of the given closure.
	 */
	void frame(Closure<Framebuffer> closure) {

		imGuiGl3.newFrame()
		imGuiGlfw.newFrame()
		ImGui.newFrame()

		// Draw scene
		var sceneResult = closure()

		// Draw ImGui objects
		average('ImGui Rendering', 1f, logger) { ->
			if (drawUiElements && sceneResult) {
				var dockspaceId = setUpDockspace()
				chrome*.render(dockspaceId, sceneResult)
			}
			if (drawOverlays) {
				overlays*.render()
			}
			ImGui.render()
			imGuiGl3.renderDrawData(ImGui.getDrawData())
		}
	}

	/**
	 * Return whether or not ImGui is the render target.
	 */
	boolean isEnabled() {

		return drawUiElements
	}

	/**
	 * Alias for {@link #addOverlay}.
	 */
	void leftShift(ImGuiOverlay overlay) {

		addOverlay(overlay)
	}

	/**
	 * Alias for {@link #addChrome}.
	 */
	void leftShift(ImGuiChrome chrome) {

		addChrome(chrome)
	}

	/**
	 * Build the docking window into which the app will be rendered.
	 */
	private static int setUpDockspace() {

		var viewport = ImGui.getMainViewport()
		ImGui.setNextWindowPos(0, 0)
		ImGui.setNextWindowSize(viewport.sizeX, viewport.sizeY)
		ImGui.pushStyleVar(WindowBorderSize, 0)
		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.pushStyleVar(WindowRounding, 0)

		ImGui.begin('DockingWindow', new ImBoolean(true),
			NoTitleBar | NoCollapse | NoResize | NoMove | NoBringToFrontOnFocus | NoNavFocus | MenuBar | NoDocking | NoBackground)
		ImGui.popStyleVar(3)

		var dockspaceId = ImGui.getID('MyDockspace')
		ImGui.dockSpace(dockspaceId, viewport.workSizeX, viewport.workSizeY, PassthruCentralNode)

		ImGui.end()

		return dockspaceId
	}

	/**
	 * When window chrome is enabled, the scene will be rendered to an ImGui image
	 * texture held by this class.
	 *
	 * @author Emanuel Rabina
	 */
	@TupleConstructor
	class GameWindow implements ImGuiChrome, EventTarget {

		final float targetAspectRatio

		boolean focused
		boolean hovered

		private Dimension lastWindowSize

		@Override
		void render(int dockspaceId, Framebuffer sceneFramebufferResult) {

			ImGui.setNextWindowSize(800, 600, FirstUseEver)
			ImGui.pushStyleVar(WindowPadding, 0, 0)
			ImGui.begin('Game', new ImBoolean(true), NoCollapse | NoScrollbar | NoScrollWithMouse)
			ImGui.popStyleVar()

			focused = ImGui.isWindowFocused()
			hovered = ImGui.isWindowHovered()

			imgui.internal.ImGui.dockBuilderDockWindow('Game', dockspaceId)
			imgui.internal.ImGui.dockBuilderFinish(dockspaceId)

			var framebufferSize = sceneFramebufferResult.texture.size
			var windowSize = new Dimension(ImGui.contentRegionMaxX as int, ImGui.contentRegionMaxY as int)
			var imageSizeX = windowSize.width()
			var imageSizeY = windowSize.height()
			var uvX = 0f
			var uvY = 0f
			var cursorX = 0
			var cursorY = ImGui.getCursorPosY()

			// Window is wider
			if (windowSize.aspectRatio > framebufferSize.aspectRatio) {
				uvX = 1 / (framebufferSize.width() - (framebufferSize.width() - windowSize.width())) as float
				imageSizeX = imageSizeY * framebufferSize.aspectRatio as float
				cursorX = (windowSize.width() - imageSizeX) * 0.5f as float
			}
			// Window is taller
			else if (windowSize.aspectRatio < framebufferSize.aspectRatio) {
				uvY = 1 / (framebufferSize.height() - (framebufferSize.height() - windowSize.height())) as float
				imageSizeY = imageSizeX / framebufferSize.aspectRatio as float
				cursorY = cursorY + (windowSize.height() - imageSizeY) * 0.5f as float
			}

			ImGui.setCursorPos(cursorX, cursorY)
			ImGui.image(((OpenGLTexture)sceneFramebufferResult.texture).textureId, imageSizeX, imageSizeY,
				uvX, 1 - uvY as float, 1 - uvX as float, uvY)

			ImGui.end()

			if (windowSize != lastWindowSize) {
				logger.debug('Scene window changed to {}', windowSize)
				var targetResolution = windowSize.calculateFit(targetAspectRatio)
				logger.debug('Target resolution changed to {}', targetResolution)
				trigger(new FramebufferSizeEvent(windowSize, windowSize, targetResolution))
				lastWindowSize = windowSize
			}
		}
	}
}
