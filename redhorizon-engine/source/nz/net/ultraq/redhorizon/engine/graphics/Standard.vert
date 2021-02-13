#version 330 core

in vec4 colour;
in vec4 position;
in vec2 texCoord;

out vec4 vertexColour;
out vec2 v_texCoord;

//uniform mat4 model;
//uniform mat4 view;
uniform mat4 projection;

void main() {
	vertexColour = colour;
//	mat4 mvp = projection * view * model;
//	gl_Position = mvp * vec4(position, 1.0);
	gl_Position = projection * position;
	v_texCoord = texCoord;
}
