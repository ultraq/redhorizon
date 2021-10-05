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
uniform mat4 model;

/**
 * Vertex shader main function, passes geometry information unaffected by the
 * camera position along to the fragment shader.
 */
void main() {

	gl_Position = model * position;
	v_vertexColour = colour;
	v_textureUVs = textureUVs;
	v_textureUnit = textureUnit;
}