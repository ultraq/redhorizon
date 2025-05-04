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

package nz.net.ultraq.redhorizon.cli

import nz.net.ultraq.preferences.Preferences
import nz.net.ultraq.redhorizon.explorer.ExplorerOptions

import picocli.CommandLine.Command
import picocli.CommandLine.IDefaultValueProvider
import picocli.CommandLine.Model.ArgSpec
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * CLI wrapper for launching the Red Horizon Explorer 🔎
 *
 * @author Emanuel Rabina
 */
@Command(name = 'explorer', defaultValueProvider = DefaultOptionsProvider)
class Explorer implements Callable<Integer> {

	static {
		System.setProperty('logback.configurationFile', 'logback-application.xml')
	}

	private static final Preferences userPreferences = new Preferences()

	@Spec
	CommandSpec commandSpec

	@Parameters(index = '0', defaultValue = Option.NULL_VALUE, description = 'Path to a file to open on launch')
	File file

	@Option(names = '--maximized', description = 'Start the application maximized. Remembers your last usage.')
	boolean maximized

	@Option(names = '--touchpad-input', description = 'Start the application using touchpad controls.  Remembers your last usage.')
	boolean touchpadInput

	@Override
	Integer call() {

		// Load options from command line or preferences
		var explorerOptions = new ExplorerOptions(
			maximized: maximized,
			touchpadInput: touchpadInput
		)

		new nz.net.ultraq.redhorizon.explorer.Explorer(commandSpec.parent().version()[0], explorerOptions, file)

		// Save preferences for next time
		userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, explorerOptions.maximized)
		userPreferences.set(ExplorerPreferences.TOUCHPAD_INPUT, explorerOptions.touchpadInput)

		return 0
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
}
