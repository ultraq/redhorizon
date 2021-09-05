#version 410 core

in vec4 v_vertexColour;
in float v_textureUnit;
in vec2 v_texelPosition;
in vec2 v_textureScale;

out vec4 fragmentColour;

uniform sampler2D textures[MAX_TEXTURE_UNITS];
uniform vec2 textureSourceSize;

/**
 * Fragment shader main function, uses a texture scaler that samples textures
 * such that they have the appearance of being integer-scaled to their target
 * size.  Made to work in conjunction with hardware bilinear filtering to smooth
 * out the transitions between pixels, while appearing sharp.
 * 
 * Based on the `sharp-bilinear-simple` shader made for RetroArch/LibRetro,
 * found here: https://github.com/rsn8887/Sharp-Bilinear-Shaders
 */
void main() {

	vec2 texel = v_texelPosition;
	vec2 scale = v_textureScale;

	vec2 texelFloor = floor(texel);
	vec2 texelFract = fract(texel);
	vec2 regionRange = 0.5 - 0.5 / scale;
	vec2 centerDist = texelFract - 0.5;
	vec2 texelOffset = (centerDist - clamp(centerDist, -regionRange, regionRange)) * scale + 0.5;
	vec2 targetUVs = (texelFloor + texelOffset) / textureSourceSize;

	fragmentColour = texture(textures[int(v_textureUnit)], targetUVs);
	fragmentColour *= v_vertexColour;
}
