#version 410 core

in vec4 v_vertexColour;

out vec4 fragmentColour;

/**
 * Fragment shader for rendering OpenGL mesh-only objects.
 */
void main() {

	fragmentColour = v_vertexColour;
}
