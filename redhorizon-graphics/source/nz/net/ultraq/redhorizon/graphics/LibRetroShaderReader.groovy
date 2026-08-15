/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics

/**
 * Extract the vertex, geometry, and fragment shader sources from a combined
 * shader file, in the RetroArch/LibRetro format.  The format is
 * <a href="https://github.com/libretro/slang-shaders/tree/master?tab=readme-ov-file#initial-preprocess-of-slang-files">outlined
 * here</a>, though currently the only thing supported is the {@code #pragma stage}
 * directive for specifying the shader type.
 *
 * @author Emanuel Rabina
 */
class LibRetroShaderReader {

	Tuple3<String, String, String> read(String shaderSourcePath) {

		return getResourceAsStream(shaderSourcePath).withBufferedReader { reader ->
			var vertexShaderSource = new StringBuilder()
			var geometryShaderSource = new StringBuilder()
			var fragmentShaderSource = new StringBuilder()
			var geometryShaderEnabled = false
			var stage = 'none'

			reader.eachLine { line ->
				if (line.startsWith('#pragma stage')) {
					stage = line.substring('#pragma stage'.length()).trim()
					if (stage == 'geometry') {
						geometryShaderEnabled = true
					}
				}
				else if (stage == 'vertex') {
					vertexShaderSource << line << '\n'
				}
				else if (stage == 'geometry') {
					geometryShaderSource << line << '\n'
				}
				else if (stage == 'fragment') {
					fragmentShaderSource << line << '\n'
				}
				else {
					vertexShaderSource << line << '\n'
					geometryShaderSource << line << '\n'
					fragmentShaderSource << line << '\n'
				}
			}

			return new Tuple3<>(
				vertexShaderSource.toString(),
				geometryShaderEnabled ? geometryShaderSource.toString() : null,
				fragmentShaderSource.toString()
			)
		}
	}
}
