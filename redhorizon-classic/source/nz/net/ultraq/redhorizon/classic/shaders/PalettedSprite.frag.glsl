#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;

out vec4 fragmentColour;

layout (packed) uniform Palette {
	vec4[256] palette;
	vec4[256] alphaMask;
};
uniform sampler2D indexTexture;
uniform int[256] adjustmentMap;

/**
 * Fragment shader main function, emits the fragment colour for a paletted
 * texture by combining the lookup value in the main texture data with a palette
 * in this shader's uniforms.
 */
void main() {

	// The final colour is obtained from multiple passes:
	//  - index value sampled from the texture
	//  - run through an adjustment map (eg: for different faction colours)
	//  - a colour is then pulled from the palette
	//  - where an alpha mask is applied
	//  - (and then the usual step of applying the vertex colouring)
	int index = int(texture(indexTexture, v_textureUVs).x * 256);
	index = adjustmentMap[index];
	vec4 colour = palette[index];
	colour *= alphaMask[index];

	fragmentColour = colour * v_vertexColour;
}
