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

in vec4 position;
in vec4 colour;

//out vec4 v_vertexColour;

//layout (std140) uniform Camera {
//	mat4 projection;
//	mat4 view;
//};
uniform mat4 model;

/**
 * A simple vertex shader that can be used to render the primitives provided in
 * the graphics module.
 */
void main() {

//	gl_Position = projection * view * model * position;
	gl_Position = model * position;
//	v_vertexColour = colour;
//	v_textureUVs = textureUVs;
}
