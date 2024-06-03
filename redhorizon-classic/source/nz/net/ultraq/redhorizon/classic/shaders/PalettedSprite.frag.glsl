#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;

out vec4 fragmentColour;

uniform sampler2D indexTexture;
uniform sampler2D palette;
uniform int[256] adjustmentMap;
uniform sampler2D alphaMask;

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
	float index = texture(indexTexture, v_textureUVs).x;
	index = float(adjustmentMap[int(index * 256)]) / 256;
	vec4 colour = texture(palette, vec2(index, 0));
	colour *= texture(alphaMask, vec2(index, 0));

	fragmentColour = colour * v_vertexColour;
}
