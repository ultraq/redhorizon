#version 410 core

layout (location = 0) in vec4 colour;
layout (location = 1) in vec4 position;
layout (location = 2) in vec2 textureUVs;
layout (location = 3) in float textureUnit;
layout (location = 4) in float modelIndex;

out vec4 v_vertexColour;
out vec2 v_textureUVs;
out float v_textureUnit;

layout (std140) uniform Camera {
	mat4 projection;
	mat4 view;
};
uniform mat4 models[maxTransforms];

void main() {
	v_vertexColour = colour;
	gl_Position = projection * view * models[int(modelIndex)] * position;
	v_textureUVs = textureUVs;
	v_textureUnit = textureUnit;
}
