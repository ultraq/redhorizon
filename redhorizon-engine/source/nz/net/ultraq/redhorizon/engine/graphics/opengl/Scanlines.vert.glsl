#version 410 core

#define outputSize vec2(OUTPUT_RESOLUTION_WIDTH, OUTPUT_RESOLUTION_HEIGHT)
#define pi 3.141592654

layout (location = 0) in vec4 colour;
layout (location = 1) in vec4 position;
layout (location = 2) in vec2 textureUVs;
layout (location = 3) in float textureUnit;
layout (location = 4) in float modelIndex;

out vec4 v_vertexColour;
out vec2 v_textureUVs;
out float v_textureUnit;
out vec2 v_omega;

layout (std140) uniform Camera {
	mat4 projection;
	mat4 view;
};
uniform mat4 models[MAX_TRANSFORMS];
uniform vec2 textureSourceSize;

/**
 * Vertex shader main function, mostly passes geometry information along to the
 * fragment shader.
 */
void main() {

	gl_Position = projection * view * models[int(modelIndex)] * position;
	v_vertexColour = colour;
	v_textureUVs = textureUVs;
	v_textureUnit = textureUnit;

	v_omega = vec2(pi * outputSize.x, 2.0 * pi * textureSourceSize.y);
}
