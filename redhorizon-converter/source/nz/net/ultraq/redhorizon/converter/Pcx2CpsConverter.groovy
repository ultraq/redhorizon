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

package nz.net.ultraq.redhorizon.converter

import nz.net.ultraq.redhorizon.filetypes.PcxFile

/**
 * PCX -> CPS file converter.
 *
 * @author Emanuel Rabina
 */
class Pcx2CpsConverter extends Converter<PcxFile, Void> {

	Pcx2CpsConverter(InputStream inputStream) {

		super(new PcxFile(inputStream))
	}

	@Override
	void convert(OutputStream outputStream, Void options) {

		new CpsFileWriter(inputFile).write(outputStream)
	}
}
