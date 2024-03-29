#version 410 core

in vec4 position;
in vec4 colour;

out vec4 v_vertexColour;

layout (std140) uniform Camera {
	mat4 projection;
	mat4 view;
};
uniform mat4 model;

/**
 * Vertex shader for rendering OpenGL mesh-only objects.
 */
void main() {

	gl_Position = projection * view * model * position;
	v_vertexColour = colour;
}
