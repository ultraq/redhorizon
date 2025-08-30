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
 * Extract the vertex and fragment shader sources from the shader file that uses
 * the RetroArch/LibRetro format.  The format is
 * <a href="https://github.com/libretro/slang-shaders/tree/master?tab=readme-ov-file#initial-preprocess-of-slang-files">outlined
 * here</a>, though currently the only thing supported is the {@code #pragma stage}
 * directive for specifying the shader type.
 *
 * @author Emanuel Rabina
 */
class LibRetroShaderReader {

	Tuple2<String, String> read(String shaderSourcePath) {

		return getResourceAsStream(shaderSourcePath).withBufferedReader { reader ->
			var vertexShaderSource = new StringBuilder()
			var fragmentShaderSource = new StringBuilder()
			var stage = 'none'
			reader.eachLine { line ->
				if (line.startsWith('#pragma stage')) {
					stage = line.substring('#pragma stage'.length()).trim()
				}
				else if (stage == 'vertex') {
					vertexShaderSource << line << '\n'
				}
				else if (stage == 'fragment') {
					fragmentShaderSource << line << '\n'
				}
				else {
					vertexShaderSource << line << '\n'
					fragmentShaderSource << line << '\n'
				}
			}
			return new Tuple2<String, String>(vertexShaderSource.toString(), fragmentShaderSource.toString())
		}
	}
}
