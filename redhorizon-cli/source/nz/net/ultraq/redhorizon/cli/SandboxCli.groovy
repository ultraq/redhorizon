/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.sandbox.Sandbox

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * CLI wrapper for launching the sandbox program.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'sandbox')
class SandboxCli implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(SandboxCli)

	@Spec
	CommandSpec commandSpec

	@Option(names = ['--touchpad-input'], description = 'Use touchpad scroll gestures to navigate')
	boolean touchpadInput

	@Override
	Integer call() throws Exception {

		Thread.currentThread().name = 'Sandbox [main]'
		logger.info('Red Horizon Sandbox {}', commandSpec.parent().version()[0])

		new Sandbox(touchpadInput).start()

		return 0
	}
}
