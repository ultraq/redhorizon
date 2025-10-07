/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This shader takes an existing output framebuffer of a low-resolution scene,
 * then upscales it using a texture scaler that samples that framebuffer such
 * that it has the appearance of being integer-scaled to their target size.
 * Made to work in conjunction with hardware bilinear filtering to smooth out
 * the transitions between pixels, while appearing sharp.
 *
 * Based on the `sharp-bilinear-simple` shader made for RetroArch/LibRetro,
 * found here: https://github.com/rsn8887/Sharp-Bilinear-Shaders
 */

#version 410 core

#pragma stage vertex
in vec4 position;
in vec4 colour;
in vec2 textureUVs;
out vec4 v_vertexColour;
out vec2 v_texelPosition;
out vec2 v_textureScale;
uniform vec2 textureSourceSize;
uniform vec2 textureTargetSize;

void main() {

	gl_Position = position;
	v_vertexColour = colour;
	v_texelPosition = textureUVs * textureSourceSize;
	v_textureScale = max(floor(textureTargetSize / textureSourceSize), vec2(1.0, 1.0));
}

#pragma stage fragment
in vec4 v_vertexColour;
in vec2 v_texelPosition;
in vec2 v_textureScale;
out vec4 fragmentColour;
uniform sampler2D framebuffer;
uniform vec2 textureSourceSize;

void main() {

	vec2 texel = v_texelPosition;
	vec2 scale = v_textureScale;
	vec2 texelFloor = floor(texel);
	vec2 texelFract = fract(texel);
	vec2 regionRange = 0.5 - 0.5 / scale;
	vec2 centerDist = texelFract - 0.5;
	vec2 texelOffset = (centerDist - clamp(centerDist, -regionRange, regionRange)) * scale + 0.5;
	vec2 targetUVs = (texelFloor + texelOffset) / textureSourceSize;

	fragmentColour = texture(framebuffer, targetUVs);
	fragmentColour *= v_vertexColour;
}
