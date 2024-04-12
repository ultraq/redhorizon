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

package nz.net.ultraq.redhorizon.cli.converter

import nz.net.ultraq.redhorizon.classic.filetypes.CpsFile

import com.github.valfirst.slf4jtest.TestLoggerFactory
import spock.lang.Ignore
import spock.lang.Specification
import static com.github.valfirst.slf4jtest.Assertions.assertThat

/**
 * Tests for the PCX -> CPS converter.
 *
 * @author Emanuel Rabina
 */
class Pcx2CpsConverterCliTests extends Specification {

	private final logger = TestLoggerFactory.getTestLogger(Pcx2CpsConverterCli)

	@Ignore("CPS conversion got busted somewhere along the way 😢")
	def "Converts a PCX file to a CPS file"() {
		given:
			var pcxPath = 'nz/net/ultraq/redhorizon/cli/converter/alipaper.pcx'
			var cpsPath = 'nz/net/ultraq/redhorizon/cli/converter/alipaper.cps'
			var converter = new Pcx2CpsConverterCli(
				sourceFile: getResourceAsFile(pcxPath),
				destFile: new File("${System.getProperty('user.dir')}/build/classes/test/${cpsPath}"),
				overwrite: true
			)

		when:
			var exitCode = converter.call()

		then:
			exitCode == 0

			// File must be able to be read back
			var cpsFile = new CpsFile(getResourceAsStream(cpsPath))
			cpsFile.palette != null
			cpsFile.imageData != null
	}

	def "Source file not found"() {
		given:
			var converter = new Pcx2CpsConverterCli(
				sourceFile: Mock(File) {
					toString() >> 'not-a-file.pcx'
				}
			)

		when:
			var exitCode = converter.call()

		then:
			exitCode == 1
			assertThat(logger).hasLogged(event -> event.formattedMessage == 'not-a-file.pcx not found')
	}

	def "Destination file already exists"() {
		given:
			var converter = new Pcx2CpsConverterCli(
				sourceFile: Mock(File) {
					exists() >> true
				},
				destFile: Mock(File) {
					exists() >> true
					toString() >> 'existing-file.cps'
				})

		when:
			var exitCode = converter.call()

		then:
			exitCode == 1
			assertThat(logger).hasLogged(event -> event.formattedMessage == 'Output file, existing-file.cps, already exists')
	}
}
