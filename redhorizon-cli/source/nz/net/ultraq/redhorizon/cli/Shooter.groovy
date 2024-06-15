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

package nz.net.ultraq.redhorizon.cli

import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * CLI wrapper for the shooter game ✈️🔫
 *
 * @author Emanuel Rabina
 */
@Command(name = 'shooter')
class Shooter implements Callable<Integer> {

	@Spec
	CommandSpec commandSpec

	@Override
	Integer call() {

		new nz.net.ultraq.redhorizon.shooter.Shooter(commandSpec.parent().version()[0])
		return 0
	}
}
