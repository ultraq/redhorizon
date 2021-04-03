#version 410 core

layout (location = 0) in vec4 colour;
layout (location = 1) in vec4 position;
layout (location = 2) in vec2 texCoord;
layout (location = 3) in float texUnit;
layout (location = 4) in float texLayer;
layout (location = 5) in float modelIndex;

out vec4 v_vertexColour;
out vec2 v_texCoord;
out float v_texUnit;
out float v_texLayer;

layout (std140) uniform Camera {
	mat4 projection;
	mat4 view;
};
uniform mat4 models[maxTextureUnits];

void main() {
	v_vertexColour = colour;
	gl_Position = projection * view * models[int(modelIndex)] * position;
	v_texCoord = texCoord;
	v_texUnit = texUnit;
	v_texLayer = texLayer;
}
