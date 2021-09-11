#version 410 core

layout (location = 0) in vec4 colour;
layout (location = 1) in vec4 position;
layout (location = 2) in vec2 textureUVs;
layout (location = 3) in float textureUnit;
layout (location = 4) in float modelIndex;

out vec4 v_vertexColour;
out vec2 v_texelPosition;
out vec2 v_textureScale;

uniform mat4 model;
uniform vec2 textureSourceSize;
uniform vec2 textureTargetSize;

/**
 * Vertex shader main function, mostly passes geometry information along to the
 * fragment shader.
 */
void main() {

	gl_Position = model * position;
	v_vertexColour = colour;

	v_texelPosition = textureUVs * textureSourceSize;
	v_textureScale = max(floor(textureTargetSize / textureSourceSize), vec2(1.0, 1.0));
}
