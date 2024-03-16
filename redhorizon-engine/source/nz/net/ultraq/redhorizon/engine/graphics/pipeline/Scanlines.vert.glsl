#version 410 core

#define pi 3.141592654

layout(location = 0) in vec4 position;
layout(location = 1) in vec4 colour;
layout(location = 2) in vec2 textureUVs;

out vec4 v_vertexColour;
out vec2 v_textureUVs;
out vec2 v_omega;

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
	v_textureUVs = textureUVs;

	v_omega = vec2(pi * textureTargetSize.x, 2.0 * pi * textureSourceSize.y);
}
