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

package nz.net.ultraq.redhorizon.converter

import nz.net.ultraq.redhorizon.classic.filetypes.CpsFile
import nz.net.ultraq.redhorizon.filetypes.PcxFile

import spock.lang.Ignore
import spock.lang.Specification

/**
 * Tests for the PNG -> SHP file converter.
 *
 * @author Emanuel Rabina
 */
class Pcx2ShpConverterTests extends Specification {

	@Ignore("CPS conversion got busted somewhere along the way 😢")
	def "Converts a PCX file to a CPS file"() {
		given:
			var pcxPath = 'nz/net/ultraq/redhorizon/converter/alipaper.pcx'
			var pcxStream = getResourceAsStream(pcxPath)
			var cpsPath = 'nz/net/ultraq/redhorizon/converter/alipaper.cps'
			var cpsStream = new File("${System.getProperty('user.dir')}/build/classes/test/${cpsPath}").newOutputStream()

		when:
			new Pcx2CpsConverter(new PcxFile(pcxStream)).convert(cpsStream)

		then:
			// File must be able to be read back
			var cpsFile = new CpsFile(getResourceAsStream(cpsPath))
			cpsFile.palette != null
			cpsFile.imageData != null

		cleanup:
			pcxStream.close()
			cpsStream.close()
	}
}
