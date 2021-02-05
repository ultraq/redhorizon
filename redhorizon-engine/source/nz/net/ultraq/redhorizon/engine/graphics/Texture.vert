#version 330 core

in vec2 texCoord;
in vec4 position;

out vec2 v_texCoord;

void main() {
	v_texCoord = texCoord;
	gl_Position = position;
}
