#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;

out vec4 fragmentColour;

uniform sampler2D indexTexture;
uniform sampler2D paletteTexture;
uniform int[16] factionColours;

// The first and last index of the faction colours in the C&C palette, adjusted
// for the 0.0f-1.0f range that texture coordinates are in
float factionLower = 0.3125f;     // 80 / 256 
float factionUpper = 0.37109375f; // 95 / 256

/**
 * Fragment shader main function, emits the fragment colour for a paletted
 * texture by combining the lookup value in the main texture data with a palette
 * in this shader's uniforms.
 */
void main() {

	float index = texture(indexTexture, v_textureUVs).x;

	// Faction-adjusted index
	if (factionLower <= index && index <= factionUpper) {
		index = float(factionColours[int(index * 256 - 80)]) / 256;
	}

	fragmentColour = texture(paletteTexture, vec2(index, 0));
	fragmentColour *= v_vertexColour;
}
