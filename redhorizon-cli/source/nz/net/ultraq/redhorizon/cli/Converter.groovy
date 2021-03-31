/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.cli.converter.Pcx2CpsConverter
import nz.net.ultraq.redhorizon.cli.converter.Png2ShpConverter

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.ParameterException
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * CLI tool for converting from one file format to another.
 * 
 * @author Emanuel Rabina
 */
@Command(
	name = 'convert',
	header = [
		'',
		'Red Horizon Utilities - File Converter',
		'======================================',
		''
	],
	mixinStandardHelpOptions = true,
	subcommands = [
	  Pcx2CpsConverter,
		Png2ShpConverter
	],
	synopsisSubcommandLabel = 'COMMAND',
	version = '${sys:redhorizon.version}'
)
class Converter implements Callable<Integer> {

	@Spec
	CommandSpec commandSpec

	/**
	 * If invoked, then a subcommand hasn't been specified and so an error is
	 * thrown.
	 * 
	 * @return
	 */
	@Override
	Integer call() {

		throw new ParameterException(commandSpec.commandLine(), 'Conversion subcommand required')
	}

	/**
	 * Bootstrap the application using Picocli.
	 * 
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(new CommandLine(new Converter()).execute(args))
	}
}
