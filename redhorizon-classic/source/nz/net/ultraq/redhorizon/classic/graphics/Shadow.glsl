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

#pragma stage vertex
in vec4 position;
in vec4 colour;
in vec2 textureUVs;
out vec4 v_vertexColour;
out vec2 v_textureUVs;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform vec2 frameXY;

void main() {
	gl_Position = projection * view * model * position;
	v_vertexColour = colour;
	v_textureUVs = textureUVs + frameXY;
}

#pragma stage fragment
in vec4 v_vertexColour;
in vec2 v_textureUVs;
out vec4 fragmentColour;
uniform sampler2D indexTexture;

void main() {
	// The final colour is obtained from:
	//  - index value sampled from the texture
	//  - run through an adjustment map (eg: for different faction colours)
	//  - a colour is then pulled from the palette
	//  - where an alpha mask is applied
	//  - (and then the usual step of applying the vertex colouring)
	vec2 index = vec2(texture(indexTexture, v_textureUVs).x, 1);
	vec4 colour = vec4(0, 0, 0, index == 0 ? 0 : 0.5);
	fragmentColour = colour * v_vertexColour;
}
