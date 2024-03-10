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
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.ImGuiElement
import nz.net.ultraq.redhorizon.engine.input.InputSource
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.events.EventTarget
import static nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent.EVENT_TYPE_STOP

import imgui.ImFont
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
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

import groovy.transform.TupleConstructor
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Wrapper around all of the `imgui-java` binding classes, hiding all of the
 * setup needed to make it work.
 *
 * @author Emanuel Rabina
 */
class ImGuiLayer implements AutoCloseable, InputSource {

	private static final Logger logger = LoggerFactory.getLogger(ImGuiLayer)

	static final String OPTIONS_DEBUG_OVERLAY = 'options/debug-overlay'
	static final String OPTIONS_SHADER_SCANLINES = 'options/scanlines-shader'
	static final String OPTIONS_SHADER_SHARP_UPSCALING = 'options/sharp-upscaling-shader'

	static ImFont robotoFont
	static ImFont robotoMonoFont

	static {

		// Extract and use the locally built natives for macOS running M processors
		if (System.isMacOs() && System.isArm64()) {
			ImGuiLayer.classLoader.getResourceAsStream('io/imgui/java/native-bin/libimgui-javaarm64.dylib').withStream { inputStream ->
				var tmpDir = File.createTempDir('imgui-java-natives-macos-arm64')
				tmpDir.deleteOnExit()
				var libFile = new File(tmpDir, 'libimgui-javaarm64.dylib')
				Files.copy(inputStream, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
				System.load(libFile.absolutePath)
			}
		}
	}

	final MainMenu mainMenu

	private final GraphicsConfiguration config
	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw
	private final GameWindow gameWindow

	private boolean drawChrome
	private boolean debugOverlay
	private boolean shaderScanlines
	private boolean shaderSharpUpscaling

	/**
	 * Create a new ImGui renderer to work with an existing OpenGL window and
	 * renderer.
	 */
	ImGuiLayer(GraphicsConfiguration config, Window window) {

		this.config = config
		debugOverlay = config.debug
		drawChrome = config.startWithChrome
		shaderScanlines = config.scanlines
		shaderSharpUpscaling = true

		imGuiGlfw = new ImGuiImplGlfw()
		imGuiGl3 = new ImGuiImplGl3()
		ImGui.createContext()

		var io = ImGui.getIO()
		io.setConfigFlags(DockingEnable)

		robotoFont = getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/imgui/Roboto-Medium.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, 16 * window.monitorScale as float)
		}
		robotoMonoFont = getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/imgui/RobotoMono-Medium.ttf').withCloseable { stream ->
			return io.fonts.addFontFromMemoryTTF(stream.bytes, 16 * window.monitorScale as float)
		}

		imGuiGlfw.init(window.handle, true)
		imGuiGl3.init('#version 410 core')

		mainMenu = new MainMenu()
		gameWindow = new GameWindow(config.targetAspectRatio)

		window.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				if (event.key == GLFW_KEY_O) {
					drawChrome = !drawChrome
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
	 * Automatically mark the beginning and end of a frame as before and after the
	 * execution of the given closure.
	 */
	void frame(Closure closure) {

		imGuiGlfw.newFrame()
		ImGui.newFrame()

		closure()

		ImGui.render()
		imGuiGl3.renderDrawData(ImGui.getDrawData())
	}

	/**
	 * Draw all of the ImGui elements to the screen.
	 */
	int render(Framebuffer sceneFramebufferResult) {

		if (drawChrome) {
			var dockspaceId = setUpDockspace()
			mainMenu.render(dockspaceId, sceneFramebufferResult)
			gameWindow.render(dockspaceId, sceneFramebufferResult)
			return dockspaceId
		}

		return -1
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
	 * An ImGUI implementation of the main menu bar.
	 *
	 * @author Emanuel Rabina
	 */
	private class MainMenu implements nz.net.ultraq.redhorizon.engine.graphics.MainMenu, ImGuiElement, EventTarget {

		@Override
		void render(int dockspaceId, Framebuffer sceneFramebufferResult) {

			if (ImGui.beginMainMenuBar()) {

				if (ImGui.beginMenu('File')) {
					if (ImGui.menuItem('Exit', 'Esc')) {
						trigger(new GuiEvent(EVENT_TYPE_STOP))
					}
					ImGui.endMenu()
				}

				if (ImGui.beginMenu('Options')) {
					if (ImGui.menuItem('Debug overlay', 'D', debugOverlay)) {
						debugOverlay = !debugOverlay
						trigger(new ChangeEvent(OPTIONS_DEBUG_OVERLAY, null))
					}
					if (ImGui.menuItem('Scanlines', 'S', shaderScanlines)) {
						shaderScanlines = !shaderScanlines
						trigger(new ChangeEvent(OPTIONS_SHADER_SCANLINES, shaderScanlines))
					}
					if (ImGui.menuItem('Sharp upscaling', 'U', shaderSharpUpscaling)) {
						shaderSharpUpscaling = !shaderSharpUpscaling
						trigger(new ChangeEvent(OPTIONS_SHADER_SHARP_UPSCALING, shaderSharpUpscaling))
					}

					if (optionsMenu) {
						ImGui.separator()
						optionsMenu*.render()
					}

					ImGui.endMenu()
				}

				ImGui.endMainMenuBar()
			}
		}
	}

	/**
	 * When window chrome is enabled, the scene will be rendered to an ImGui image
	 * texture held by this class.
	 *
	 * @author Emanuel Rabina
	 */
	@TupleConstructor
	private class GameWindow implements ImGuiElement, EventTarget {

		final float targetAspectRatio

		private Dimension lastWindowSize

		@Override
		void render(int dockspaceId, Framebuffer sceneFramebufferResult) {

			ImGui.setNextWindowSize(800, 600, FirstUseEver)
			ImGui.pushStyleVar(WindowPadding, 0, 0)
			ImGui.begin('Game', new ImBoolean(true), NoCollapse | NoScrollbar)
			ImGui.popStyleVar()

			imgui.internal.ImGui.dockBuilderDockWindow('Game', dockspaceId)
			imgui.internal.ImGui.dockBuilderFinish(dockspaceId)

			var framebufferSize = sceneFramebufferResult.texture.size
			var windowSize = new Dimension(ImGui.contentRegionMaxX as int, ImGui.contentRegionMaxY as int)
			var imageSizeX = windowSize.width
			var imageSizeY = windowSize.height
			var uvX = 0f
			var uvY = 0f
			var cursorX = 0
			var cursorY = ImGui.getCursorPosY()

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
				cursorY = cursorY + (windowSize.height - imageSizeY) * 0.5f as float
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
