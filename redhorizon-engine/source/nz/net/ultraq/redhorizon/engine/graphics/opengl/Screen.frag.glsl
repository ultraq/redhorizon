#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;

out vec4 fragmentColour;

uniform sampler2D framebuffer;

/**
 * Fragment shader main function, emits the fragment colour for a texture in a
 * given texture unit.
 */
void main() {

	fragmentColour = texture(framebuffer, v_textureUVs);
	fragmentColour *= v_vertexColour;
}
