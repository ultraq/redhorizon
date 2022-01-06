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

package nz.net.ultraq.redhorizon.explorer

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * CLI wrapper for launching the Red Horizon Explorer 🔎
 * 
 * @author Emanuel Rabina
 */
@Command(
	name = "explorer",
	header = [
		'',
		'Red Horizon Explorer 🔎',
		'======================',
		''
	],
	mixinStandardHelpOptions = true,
	version = '${sys:redhorizon.version}'
)
class ExplorerCli implements Callable<Integer> {

	@Spec
	CommandSpec commandSpec

	@Override
	Integer call() {

		new Explorer(commandSpec.version()[0] ?: '(development)').start()
		return 0
	}

	/**
	 * Bootstrap the application using Picocli.
	 * 
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(new CommandLine(new ExplorerCli()).execute(args))
	}
}
