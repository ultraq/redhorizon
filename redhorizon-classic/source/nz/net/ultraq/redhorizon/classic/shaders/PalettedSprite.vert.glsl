#version 410 core

layout(location = 0) in vec4 position;
layout(location = 1) in vec4 colour;
layout(location = 2) in vec2 textureUVs;

out vec4 v_vertexColour;
out vec2 v_textureUVs;

layout (std140) uniform Camera {
	mat4 projection;
	mat4 view;
};
uniform mat4 model;

/**
 * Vertex shader main function, mostly passes geometry information along to the
 * fragment shader.
 */
void main() {

	gl_Position = projection * view * model * position;
	v_vertexColour = colour;
	v_textureUVs = textureUVs;
}
