#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;
in float v_textureUnit;
in vec2 v_texelPosition;
in vec2 v_textureScale;

out vec4 fragmentColour;

uniform sampler2D textures[maxTextureUnits];
uniform vec2 textureSourceSize;
uniform vec2 textureTargetSize;

/**
 * A texture scaler that samples textures such that they have the appearance of
 * being integer-scaled to their target size.  Made to work in conjunction with
 * hardware bilinear filtering to smooth out the transitions between pixels,
 * while appearing sharp.
 * 
 * Based off the `sharp-bilinear-simple` shader made for RetroArch/LibRetro,
 * found here: https://github.com/rsn8887/Sharp-Bilinear-Shaders
 */
vec4 textureScale(sampler2D tex, vec2 uv) {

	vec2 texel = v_texelPosition;
	vec2 scale = v_textureScale;

	vec2 texelFloor = floor(texel);
	vec2 texelFract = fract(texel);
	vec2 regionRange = 0.5 - 0.5 / scale;
	vec2 centerDist = texelFract - 0.5;
	vec2 texelOffset = (centerDist - clamp(centerDist, -regionRange, regionRange)) * scale + 0.5;
	vec2 targetUVs = (texelFloor + texelOffset) / textureSourceSize;

	return vec4(texture(tex, targetUVs).rgb, 1.0);
}

/**
 * Fragment shader main function, emits the fragment colour for a texture in a
 * given texture unit.
 */
void main() {

	vec4 textureColour = textureScale(textures[int(v_textureUnit)], v_textureUVs);
	fragmentColour = textureColour * v_vertexColour;
}
