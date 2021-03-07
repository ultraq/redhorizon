#version 410 core

in vec4 colour;
in vec4 position;
in vec2 texCoord;
in float texUnit;

out vec4 v_vertexColour;
out vec2 v_texCoord;
out float v_texUnit;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
	v_vertexColour = colour;
	gl_Position = projection * view * model * position;
	v_texCoord = texCoord;
	v_texUnit = texUnit;
}
