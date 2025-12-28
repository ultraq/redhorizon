/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli.mixtools

import nz.net.ultraq.redhorizon.classic.filetypes.MixFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * MIX CLI command for returning the calculated ID of a file name.
 *
 * @author Emanuel Rabina
 */
@Command(
	name = 'id',
	description = 'Calculate the mix entry ID of a file name',
	mixinStandardHelpOptions = true
)
class MixIdCalculator implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(MixIdCalculator)

	@Spec
	CommandSpec commandSpec

	@Parameters(description = 'File name to return the calculated ID for')
	String[] fileNames

	@Override
	Integer call() {

		logger.info('Red Horizon Mix ID calculator {}', commandSpec.parent().parent().version()[0])

		fileNames.each { fileName ->
			var id = MixFile.calculateId(fileName)
			println("${fileName} = ${id} (0x${Integer.toHexString(id)})")
		}

		return 0
	}
}
