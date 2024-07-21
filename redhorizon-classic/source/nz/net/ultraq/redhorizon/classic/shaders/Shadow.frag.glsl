#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;

out vec4 fragmentColour;

uniform sampler2D indexTexture;

/**
 * Fragment shader main function, emits only a shadow colour for any pixel that
 * is not using the 0th palette index.
 */
void main() {

	int index = int(texture(indexTexture, v_textureUVs).x * 256);
	vec4 colour = vec4(0, 0, 0, index == 0 ? 0 : 0.5);

	fragmentColour = colour * v_vertexColour;
}
