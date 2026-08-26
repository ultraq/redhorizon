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

package nz.net.ultraq.redhorizon.explorer

import nz.net.ultraq.preferences.Preferences
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.explorer.controllers.CyclePaletteController
import nz.net.ultraq.redhorizon.explorer.filedata.FileEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixDatabase
import nz.net.ultraq.redhorizon.explorer.objects.GlobalPalette
import nz.net.ultraq.redhorizon.explorer.previews.PreviewBeginEvent
import nz.net.ultraq.redhorizon.explorer.previews.PreviewController
import nz.net.ultraq.redhorizon.explorer.previews.PreviewEndEvent
import nz.net.ultraq.redhorizon.explorer.ui.EntryList
import nz.net.ultraq.redhorizon.explorer.ui.EntrySelectedEvent
import nz.net.ultraq.redhorizon.explorer.ui.MainMenuBar
import nz.net.ultraq.redhorizon.explorer.ui.TouchpadInputEvent
import nz.net.ultraq.redhorizon.explorer.ui.UiController
import nz.net.ultraq.redhorizon.explorer.ui.UiSettingsComponent
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.graphics.opengl.SharpUpscalingShader
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.Runtime
import nz.net.ultraq.redhorizon.runtime.utilities.VersionReader
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.WINDOW

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.IDefaultValueProvider
import picocli.CommandLine.Model.ArgSpec
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Option

import java.util.concurrent.Callable

/**
 * A Command & Conquer asset explorer, allows peeking into and previewing the
 * classic C&C files using a file explorer-like interface.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'explorer', defaultValueProvider = DefaultOptionsProvider)
class Explorer extends Application implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(Explorer)
	private static final Preferences userPreferences = new Preferences()
	private static final int RENDER_WIDTH = 640
	private static final int RENDER_HEIGHT = 480
	private static final int OUTPUT_WIDTH = RENDER_WIDTH * 2
	private static final int OUTPUT_HEIGHT = RENDER_HEIGHT * 2

	/**
	 * Entry point to the Explorer application.
	 */
	static void main(String[] args) {

		System.exit(new CommandLine(new Explorer()).execute(args))
	}

	/**
	 * Provide default values for the user-remembered options.
	 */
	static class DefaultOptionsProvider implements IDefaultValueProvider {

		@Override
		String defaultValue(ArgSpec argSpec) {

			if (argSpec.option) {
				var option = (OptionSpec)argSpec
				if (option.longestName() == '--maximized') {
					return userPreferences.get(ExplorerPreferences.WINDOW_MAXIMIZED)
				}
				if (option.longestName() == '--touchpad-input') {
					return userPreferences.get(ExplorerPreferences.TOUCHPAD_INPUT).toString()
				}
				if (option.longestName() == '--starting-directory') {
					return userPreferences.get(ExplorerPreferences.STARTING_DIRECTORY).toString()
				}
			}
			return null
		}
	}

	@Option(names = '--maximized', description = 'Start the application maximized. Remembers your last usage.')
	boolean maximized

	@Option(names = '--touchpad-input', description = 'Start the application using touchpad controls.  Remembers your last usage.')
	boolean touchpadInput

	@Option(names = '--starting-directory', description = 'View this directory on launch.  Remembers your last usage.')
	File startingDirectory

	private SharpUpscalingShader sharpUpscalingShader
	private Framebuffer postProcessingFramebuffer
	private GraphicsSystem graphicsSystem

	/**
	 * Constructor, set the program name and version.
	 */
	Explorer() {

		super('Red Horizon Explorer', new VersionReader('explorer.properties').read())
	}

	@Override
	Integer call() {

		try {
			return new Runtime(this)
				.withDebugComponents(false)
				.withAudioListenerGain(0.5f)
				.withWindowWidth(OUTPUT_WIDTH)
				.withWindowHeight(OUTPUT_HEIGHT)
				.withWindowMaximized(maximized)
				.withFramebufferWidth(RENDER_WIDTH)
				.withFramebufferHeight(RENDER_HEIGHT)
				.withWindowBackgroundColour(Colour.GREY)
				.withAdditionalShaders { ->
					return [new PalettedSpriteShader()]
				}
				.execute()
		}
		finally {
			// Save preferences for next time
			userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, maximized)
			userPreferences.set(ExplorerPreferences.TOUCHPAD_INPUT, touchpadInput)
			if (startingDirectory) {
				userPreferences.set(ExplorerPreferences.STARTING_DIRECTORY, startingDirectory.toString())
			}
		}
	}

	@Override
	protected Scene configureScene(Scene scene) {

		var window = WINDOW.get()
		window.on(WindowMaximizedEvent) { event ->
			maximized = event.maximized()
		}

		scene
			.addChild(new Node()
				.withName('Debug UI')
				.addChild(new DebugOverlay()
					.withCursorTracking(window, scene.find(Camera))
					.withProfilingLogging()))
			.addChild(new Node()
				.withName('UI')
				.addChild(new MainMenuBar(window, touchpadInput)
					.withName('Main menu'))
				.addChild(new EntryList()
					.withName('Entry list'))
				.addChild(new NodeList()
					.withName('Node list'))
				.addChild(new LogPanel()
					.withName('Log panel'))
				.addChild(new UiSettingsComponent(startingDirectory, new MixDatabase(), touchpadInput)
					.withName('UI settings'))
				.addChild(new ScriptNode(UiController))
			)
			.addChild(new Node()
				.withName('Preview controller')
				.addChild(new ScriptNode(PreviewController))
			)
			.addChild(new GridLines(nz.net.ultraq.redhorizon.classic.maps.Map.MAX_BOUNDS, 24,
				new Colour('GridLines-DarkGrey', 0.2f, 0.2f, 0.2f), new Colour('GridLines-Grey', 0.6f, 0.6f, 0.6f))
				.withName('Grid lines'))
			.addChild(new GlobalPalette()
				.withName('Global palette & alpha mask'))
			.addChild(new ScriptNode(CyclePaletteController))

		scene
			.on(TouchpadInputEvent) { event ->
				touchpadInput = event.touchpadInput()
			}
			.on(EntrySelectedEvent) { event ->
				var entry = event.entry()
				if (entry instanceof FileEntry && entry.file().directory) {
					startingDirectory = entry.file()
				}
			}
			.on(PreviewBeginEvent) { event ->
				// Use sharp upscaling shader when adjusting for low-res files with an old aspect ratio
				if (event.fileName().endsWith('.wsa')) {
					logger.debug('WSA file detected, using sharp upscaling shader to fix aspect ratio issues')
					graphicsSystem.withPostProcessing { sceneBuffer ->
						postProcessingFramebuffer.useFramebuffer { ->
							sharpUpscalingShader.useShader { shaderContext ->
								shaderContext.setTextureSourceSize(320, 200)
								shaderContext.setTextureTargetSize(OUTPUT_WIDTH, OUTPUT_HEIGHT)
								sceneBuffer.draw(shaderContext)
							}
						}
					}
				}
			}
			.on(PreviewEndEvent) { event ->
				graphicsSystem.withPostProcessing(null)
			}

		return scene
	}

	@Override
	protected Engine configureEngine(Engine engine) {

		graphicsSystem = engine.findSystem(GraphicsSystem)
		sharpUpscalingShader = new SharpUpscalingShader()
		postProcessingFramebuffer = new OpenGLFramebuffer(OUTPUT_WIDTH, OUTPUT_HEIGHT, true)
		return engine
	}
}
