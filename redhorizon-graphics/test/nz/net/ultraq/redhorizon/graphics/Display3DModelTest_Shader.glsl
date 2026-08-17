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
in vec3 normal;
out VertexData {
	vec4 colour;
	vec3 normal;
	vec3 fragmentPosition;
} v;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main() {
	gl_Position = projection * view * model * position;
	v.colour = colour;
	v.normal = (model * vec4(normal, 0.0)).xyz;
	v.fragmentPosition = (model * position).xyz;
}

#pragma stage fragment
in VertexData {
	vec4 colour;
	vec3 normal;
	vec3 fragmentPosition;
} v;
out vec4 fragmentColour;
uniform vec4 ambientColour;
uniform vec4 lightColour;
uniform vec3 lightPosition;

void main() {
	vec3 norm = normalize(v.normal);
	vec3 lightDir = normalize(lightPosition - v.fragmentPosition);
	vec4 diffuse = max(dot(norm, lightDir), 0.0) * lightColour;
	fragmentColour = v.colour * (ambientColour + diffuse);
}
