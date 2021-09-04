#version 410 core

#define scanlineBaseBrightness float(1.0)
#define scanlineHorizontalModulation float(0.0)
#define scanlineVerticalModulation float(0.15)

in vec4 v_vertexColour;
in vec2 v_textureUVs;
in float v_textureUnit;
in vec2 v_omega;

out vec4 fragmentColour;

uniform sampler2D textures[MAX_TEXTURE_UNITS];

/**
 * Fragment shader main function, applies a scanline effect to an existing
 * texture.
 * 
 * Based on the scanline shaders found in RetroArch/LibRetro, found here:
 * https://github.com/libretro/glsl-shaders
 */
void main() {

	fragmentColour = texture(textures[int(v_textureUnit)], v_textureUVs);
	vec2 sineComp = vec2(scanlineHorizontalModulation, scanlineVerticalModulation);
	fragmentColour *= vec4(scanlineBaseBrightness + dot(sineComp * sin(v_textureUVs * v_omega - 1), vec2(1.0, 1.0)));
	fragmentColour *= v_vertexColour;
}
