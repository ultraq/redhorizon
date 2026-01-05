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
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.scripts.ScriptSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler

import org.lwjgl.system.Configuration
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

	private static final Preferences userPreferences = new Preferences()
	private static final int RENDER_WIDTH = 640
	private static final int RENDER_HEIGHT = 480

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
	 * Provide default options for the user-remembered options.
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

	private Window window
	private Framebuffer framebuffer
	private AudioDevice device
	private ResourceManager resourceManager
	private ExplorerScene scene

	@Override
	void run() {

		try {
			window = new OpenGLWindow(1280, 800, 'Explorer')
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.GREY)
				.withMaximized(maximized)
				.withVSync(true)
				.on(WindowMaximizedEvent) { event ->
					maximized = event.maximized()
				}
			framebuffer = new OpenGLFramebuffer(RENDER_WIDTH, RENDER_HEIGHT)
			device = new OpenALAudioDevice()
				.withMasterVolume(0.5f)
			var input = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)
			resourceManager = new ResourceManager('.')

			scene = new ExplorerScene(RENDER_WIDTH, RENDER_HEIGHT, window, resourceManager, input, touchpadInput)
			var engine = new Engine()
				.addSystem(new InputSystem(input))
				.addSystem(new GraphicsSystem(window, framebuffer, new BasicShader(), new PalettedSpriteShader()))
				.addSystem(new ScriptSystem(new ScriptEngine('.'), input))
				.withScene(scene)

			// Game loop
			window.show()
			if (file) {
				scene.preview(file)
			}
			var deltaTimer = new DeltaTimer()
			while (!window.shouldClose()) {
				engine.update(deltaTimer.deltaTime())
				Thread.yield()
			}
		}
		finally {
			resourceManager?.close()
			device?.close()
			window?.close()
		}

		// Save preferences for next time
		userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, maximized)
		userPreferences.set(ExplorerPreferences.TOUCHPAD_INPUT, touchpadInput)
	}
}
