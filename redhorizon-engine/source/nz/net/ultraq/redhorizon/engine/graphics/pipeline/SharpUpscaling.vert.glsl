#version 410 core

in vec4 position;
in vec4 colour;
in vec2 textureUVs;

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
