#version 410 core

in vec4 position;
in vec4 colour;
in vec2 textureUVs;

out vec4 v_vertexColour;
out vec2 v_textureUVs;

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
}
