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

import picocli.CommandLine.Command

/**
 * Parent command to hold all of the mix-related CLI commands.
 *
 * @author Emanuel Rabina
 */
@Command(
	name = 'mix',
	header = [
		'',
		'Red Horizon Mix Tools',
		''
	],
	description = 'Tools related to C&C mix files',
	mixinStandardHelpOptions = true,
	subcommands = [
		MixExtractor,
		MixIdCalculator
	]
)
class Mix {
}
