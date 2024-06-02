#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;

out vec4 fragmentColour;

uniform sampler2D indexTexture;
uniform sampler2D paletteTexture;
uniform int[256] factionMap;

/**
 * Fragment shader main function, emits the fragment colour for a paletted
 * texture by combining the lookup value in the main texture data with a palette
 * in this shader's uniforms.
 */
void main() {

	// Index sampled from the texture, then run through the faction colour
	// adjustment map, then used to pull a colour from the palette
	float index = texture(indexTexture, v_textureUVs).x;
	index = float(factionMap[int(index * 256)]) / 256;
	fragmentColour = texture(paletteTexture, vec2(index, 0));
	fragmentColour *= v_vertexColour;
}
