#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;

out vec4 fragmentColour;

uniform sampler2D indexTexture;
uniform sampler2D paletteTexture;

/**
 * Fragment shader main function, emits the fragment colour for a paletted
 * texture by combining the lookup value in the main texture data with a palette
 * in this shader's uniforms.
 */
void main() {

	vec4 index = texture(indexTexture, v_textureUVs);
	fragmentColour = texture(paletteTexture, index.xy);
	fragmentColour *= v_vertexColour;
}
