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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.cli.Converter
import nz.net.ultraq.redhorizon.cli.Explorer
import nz.net.ultraq.redhorizon.cli.MediaPlayer
import nz.net.ultraq.redhorizon.cli.MixReader
import nz.net.ultraq.redhorizon.cli.ObjectViewer

import picocli.CommandLine
import picocli.CommandLine.Command

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
		'===============',
		''
	],
	description = 'The Red Horizon command-line interface',
	mixinStandardHelpOptions = true,
	subcommands = [
		Converter,
		Explorer,
	  MediaPlayer,
		MixReader,
		ObjectViewer
	],
	version = '${sys:redhorizon.version}'
)
class RedHorizonCli {

	/**
	 * Bootstrap the application using Picocli.
	 * 
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(new CommandLine(new RedHorizonCli()).execute(args))
	}
}
