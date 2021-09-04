#version 410 core

#define scanlineBaseBrightness float(1.0)
#define scanlineHorizontalModulation float(0.0)
#define scanlineVerticalModulation float(0.15)

in vec4 v_vertexColour;
in vec2 v_textureUVs;
in float v_textureUnit;
in vec2 v_texelPosition;
in vec2 v_textureScale;
in vec2 v_omega;

out vec4 fragmentColour;

uniform sampler2D textures[MAX_TEXTURE_UNITS];
uniform vec2 textureSourceSize;
uniform vec2 textureTargetSize;
uniform bool useScanlines;

/**
 * A texture scaler that samples textures such that they have the appearance of
 * being integer-scaled to their target size.  Made to work in conjunction with
 * hardware bilinear filtering to smooth out the transitions between pixels,
 * while appearing sharp.
 * 
 * Based on the `sharp-bilinear-simple` shader made for RetroArch/LibRetro,
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

	return texture(tex, targetUVs);
}

/**
 * Return a value that the current colour can be multiplied by that will give
 * the appearance of scanlines across the screen.
 * 
 * Based on the scanline shaders found in RetroArch/LibRetro, found here:
 * https://github.com/libretro/glsl-shaders
 */
vec4 insertScanlines(vec2 uv) {

	vec2 sineComp = vec2(scanlineHorizontalModulation, scanlineVerticalModulation);
	return vec4(scanlineBaseBrightness + dot(sineComp * sin(uv * v_omega - 1), vec2(1.0, 1.0)));
}

/**
 * Fragment shader main function, emits the fragment colour for a texture in a
 * given texture unit.
 */
void main() {

//	fragmentColour = textureScale(textures[int(v_textureUnit)], v_textureUVs);
//	fragmentColour *= useScanlines ? insertScanlines(v_textureUVs) : vec4(1.0, 1.0, 1.0, 1.0);
//	fragmentColour *= v_vertexColour;
	fragmentColour = texture(textures[int(v_textureUnit)], v_textureUVs);
}
