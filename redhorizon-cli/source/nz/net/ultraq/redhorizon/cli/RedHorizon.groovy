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

package nz.net.ultraq.redhorizon.cli

import nz.net.ultraq.redhorizon.cli.converter.Converter
import nz.net.ultraq.redhorizon.cli.mixtools.Mix
import nz.net.ultraq.redhorizon.explorer.Explorer
import nz.net.ultraq.redhorizon.shooter.Shooter

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.IVersionProvider

/**
 * The top-level CLI program, hosting all the other programs as subcommands.
 *
 * @author Emanuel Rabina
 */
@Command(
	name = 'redhorizon',
	header = [
		'',
		'Red Horizon CLI',
		''
	],
	description = 'The Red Horizon command-line interface',
	mixinStandardHelpOptions = true,
	subcommands = [
		Converter,
		Explorer.CliWrapper,
		Mix,
		Shooter.CliWrapper
	],
	versionProvider = VersionProvider
)
class RedHorizon {

	/**
	 * Bootstrap the application using Picocli.
	 */
	static void main(String[] args) {

		System.exit(new CommandLine(new RedHorizon()).execute(args))
	}

	/**
	 * Read the version number from the {@code cli.properties} file.
	 */
	static class VersionProvider implements IVersionProvider {

		@Override
		String[] getVersion() {

			return getResourceAsStream('cli.properties').withBufferedReader { reader ->
				var cliProperties = new Properties()
				cliProperties.load(reader)
				var version = cliProperties.getProperty('version')
				return new String[]{ version == '${version}' ? '(development)' : version }
			}
		}
	}
}
