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
import nz.net.ultraq.redhorizon.audio.AudioDevice
import nz.net.ultraq.redhorizon.audio.openal.OpenALAudioDevice
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.audio.AudioSystem
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.scene.SceneChangesSystem
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.scripts.ScriptSystem
import nz.net.ultraq.redhorizon.engine.time.TimeSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.explorer.filedata.FileEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixDatabase
import nz.net.ultraq.redhorizon.explorer.previews.PreviewBeginEvent
import nz.net.ultraq.redhorizon.explorer.previews.PreviewEndEvent
import nz.net.ultraq.redhorizon.explorer.ui.EntrySelectedEvent
import nz.net.ultraq.redhorizon.explorer.ui.ExitEvent
import nz.net.ultraq.redhorizon.explorer.ui.TouchpadInputEvent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.graphics.opengl.SharpUpscalingShader
import nz.net.ultraq.redhorizon.input.InputEventHandler

import org.lwjgl.system.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.IDefaultValueProvider
import picocli.CommandLine.Model.ArgSpec
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

/**
 * A Command & Conquer asset explorer, allows peeking into and previewing the
 * classic C&C files using a file explorer-like interface.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'explorer', defaultValueProvider = DefaultOptionsProvider)
class Explorer implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Explorer)
	private static final Preferences userPreferences = new Preferences()
	private static final int RENDER_WIDTH = 640
	private static final int RENDER_HEIGHT = 480
	private static final int OUTPUT_WIDTH = RENDER_WIDTH * 2
	private static final int OUTPUT_HEIGHT = RENDER_HEIGHT * 2

	static {
		Configuration.STACK_SIZE.set(10240)
	}

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

	@Parameters(index = '0', defaultValue = Option.NULL_VALUE, description = 'Path to a file to open on launch')
	File file

	@Option(names = '--maximized', description = 'Start the application maximized. Remembers your last usage.')
	boolean maximized

	@Option(names = '--touchpad-input', description = 'Start the application using touchpad controls.  Remembers your last usage.')
	boolean touchpadInput

	@Option(names = '--starting-directory', description = 'View this directory on launch.  Remembers your last usage.')
	File startingDirectory

	private Window window
	private Framebuffer sceneFramebuffer
	private SharpUpscalingShader sharpUpscalingShader
	private Framebuffer postProcessingFramebuffer
	private AudioDevice device
	private ResourceManager resourceManager
	private ExplorerScene scene

	@Override
	void run() {

		try {
			// Setup
			window = new OpenGLWindow(OUTPUT_WIDTH, OUTPUT_HEIGHT, 'Explorer')
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.GREY)
				.withMaximized(maximized)
				.withVSync(true)
				.on(WindowMaximizedEvent) { event ->
					maximized = event.maximized()
				}
			sceneFramebuffer = new OpenGLFramebuffer(RENDER_WIDTH, RENDER_HEIGHT)
			sharpUpscalingShader = new SharpUpscalingShader()
			postProcessingFramebuffer = new OpenGLFramebuffer(OUTPUT_WIDTH, OUTPUT_HEIGHT, true)
			device = new OpenALAudioDevice()
				.withMasterVolume(0.5f)
			var input = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)
			resourceManager = new ResourceManager('.')

			// Init scene and engine
			var mixDatabase = new MixDatabase()
			scene = new ExplorerScene(window, RENDER_WIDTH, RENDER_HEIGHT, touchpadInput, startingDirectory, mixDatabase)
			scene
				.on(ExitEvent) { event ->
					window.shouldClose(true)
				}
				.on(TouchpadInputEvent) { event ->
					touchpadInput = event.touchpadInput()
				}
				.on(EntrySelectedEvent) { event ->
					var entry = event.entry()
					if (entry instanceof FileEntry && entry.file.directory) {
						startingDirectory = entry.file
					}
				}
			var graphicsSystem = new GraphicsSystem(window, sceneFramebuffer, new BasicShader(), new PalettedSpriteShader())
			scene
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
							return postProcessingFramebuffer
						}
					}
				}
				.on(PreviewEndEvent) { event ->
					graphicsSystem.withPostProcessing(null)
				}
			var engine = new Engine()
				.addSystem(new InputSystem(input))
				.addSystem(new TimeSystem())
				.addSystem(new ScriptSystem(new ScriptEngine('.'), input))
				.addSystem(new AudioSystem())
				.addSystem(graphicsSystem)
				.addSystem(new SceneChangesSystem())
				.withScene(scene)

			// Game loop
			window.show()
//			if (file) {
//				scene.preview(file)
//			}
			var deltaTimer = new DeltaTimer()
			while (!window.shouldClose()) {
				engine.update(deltaTimer.deltaTime())
				Thread.yield()
			}
		}
		finally {
			// Shutdown
			resourceManager?.close()
			device?.close()
			postProcessingFramebuffer?.close()
			sharpUpscalingShader?.close()
			window?.close()
		}

		// Save preferences for next time
		userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, maximized)
		userPreferences.set(ExplorerPreferences.TOUCHPAD_INPUT, touchpadInput)
		if (startingDirectory) {
			userPreferences.set(ExplorerPreferences.STARTING_DIRECTORY, startingDirectory.toString())
		}
	}
}
