#version 410 core

layout (location = 0) in vec4 colour;
layout (location = 1) in vec4 position;
layout (location = 3) in float texUnit;
layout (location = 4) in float modelIndex;

out vec4 v_vertexColour;

uniform mat4 models[maxTextureUnits];
uniform mat4 view;
uniform mat4 projection;

void main() {
	v_vertexColour = colour;
	gl_Position = projection * view * models[int(modelIndex)] * position;
}
