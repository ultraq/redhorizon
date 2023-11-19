#version 410 core

#define scanlineBaseBrightness float(1.0)
#define scanlineHorizontalModulation float(0.0)
#define scanlineVerticalModulation float(0.15)
#define sineComp vec2(scanlineHorizontalModulation, scanlineVerticalModulation)

in vec4 v_vertexColour;
in vec2 v_textureUVs;
in vec2 v_omega;

out vec4 fragmentColour;

uniform sampler2D framebuffer;

/**
 * Fragment shader main function, applies a scanline effect to an existing
 * texture.
 * 
 * Based on the scanline shaders found in RetroArch/LibRetro, found here:
 * https://github.com/libretro/glsl-shaders
 */
void main() {

	fragmentColour = texture(framebuffer, v_textureUVs);
	fragmentColour *= vec4(scanlineBaseBrightness + dot(sineComp * sin(v_textureUVs * v_omega - 1), vec2(1.0, 1.0)));
	fragmentColour *= v_vertexColour;
}
