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

import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsContext
import nz.net.ultraq.redhorizon.engine.input.InputSource
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.geometry.Dimension
import static nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent.*

import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImBoolean
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static imgui.flag.ImGuiConfigFlags.*
import static imgui.flag.ImGuiDockNodeFlags.*
import static imgui.flag.ImGuiStyleVar.*
import static imgui.flag.ImGuiWindowFlags.*
import static org.lwjgl.glfw.GLFW.*

/**
 * Wrapper around all of the `imgui-java` binding classes, hiding all of the
 * setup needed to make it work.
 * 
 * @author Emanuel Rabina
 */
class ImGuiLayer implements AutoCloseable, InputSource {

	private static final Logger logger = LoggerFactory.getLogger(ImGuiLayer)

	private final GraphicsConfiguration config
	private final ImGuiImplGl3 imGuiGl3
	private final ImGuiImplGlfw imGuiGlfw

	private boolean drawChrome
	private Dimension lastWindowSize
	private int dockspaceId
	private boolean docked

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
	ImGuiLayer(GraphicsConfiguration config, GraphicsContext context) {

		this.config = config
		debugOverlay = config.debug
		drawChrome = config.startWithChrome
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
	 * Draw the scene from the given framebuffer into an ImGui window that will
	 * take up the whole screen by default.
	 * 
	 * @param sceneFramebufferResult
	 */
	private void drawScene(Framebuffer sceneFramebufferResult) {

		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.begin("Scene", new ImBoolean(true), NoCollapse | NoScrollbar)
		ImGui.popStyleVar()

		if (!docked && drawChrome) {
			imgui.internal.ImGui.dockBuilderDockWindow('Scene', dockspaceId)
			imgui.internal.ImGui.dockBuilderFinish(dockspaceId)
			docked = true
		}

		def framebufferSize = sceneFramebufferResult.texture.size
		def windowSize = new Dimension(ImGui.contentRegionMaxX as int, ImGui.contentRegionMaxY as int)
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

		if (windowSize != lastWindowSize) {
			logger.debug('Scene window changed to {}', windowSize)
			def targetResolution = windowSize.calculateFit(config.targetAspectRatio)
			logger.debug('Target resolution changed to {}', targetResolution)
			trigger(new FramebufferSizeEvent(windowSize, windowSize, targetResolution))
			lastWindowSize = windowSize
		}
	}

	/**
	 * Automatically mark the beginning and end of a frame as before and after the
	 * execution of the given closure.
	 * 
	 * @param closure
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
	 * 
	 * @param sceneFramebufferResult
	 */
	void render(Framebuffer sceneFramebufferResult) {

		if (drawChrome) {
			setUpDockspace()
			drawScene(sceneFramebufferResult)
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

		ImGui.begin('DockingWindow', new ImBoolean(true),
			NoTitleBar | NoCollapse | NoResize | NoMove | NoBringToFrontOnFocus | NoNavFocus | MenuBar | NoDocking | NoBackground)
		ImGui.popStyleVar(3)

		dockspaceId = ImGui.getID('MyDockspace')
		ImGui.dockSpace(dockspaceId, viewport.workSizeX, viewport.workSizeY - 20 as float, PassthruCentralNode)

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
}
