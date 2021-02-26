#version 410 core

in vec4 colour;
in vec4 position;

out vec4 v_vertexColour;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
	v_vertexColour = colour;
	gl_Position = projection * view * model * position;
}
