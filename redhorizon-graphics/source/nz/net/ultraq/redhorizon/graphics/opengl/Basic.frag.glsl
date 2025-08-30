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

#version 410 core

in vec4 v_vertexColour;
//in vec2 v_textureUVs;

out vec4 fragmentColour;

//uniform sampler2D mainTexture;

/**
 * A simple fragment shader that can be used to render the primitives provided in
 * the graphics module.
 */
void main() {

//	fragmentColour = texture(mainTexture, v_textureUVs);
//	fragmentColour = v_vertexColour;
	fragmentColour = vec4(1, 0, 0, 1);
}
