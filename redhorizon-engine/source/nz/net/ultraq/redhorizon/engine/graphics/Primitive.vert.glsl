#version 410 core

layout (location = 0) in vec4 colour;
layout (location = 1) in vec4 position;

out vec4 v_vertexColour;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
	v_vertexColour = colour;
	gl_Position = projection * view * model * position;
}
