#version 410 core

in vec4 position;
in vec4 colour;
in vec2 textureUVs;

out vec4 v_vertexColour;
out vec2 v_textureUVs;

layout (std140) uniform Camera {
	mat4 projection;
	mat4 view;
};
uniform mat4 model;
layout (std140) uniform SpriteMetadata {
	float frameStepX;
	float frameStepY;
	int framesHorizontal;
	int framesVertical;
};
uniform int frame;

/**
 * Vertex shader main function, mostly passes geometry information along to the
 * fragment shader.
 */
void main() {

	gl_Position = projection * view * model * position;
	v_vertexColour = colour;

	// Adjust textureUVs to the location of the selected frame in the spritesheet
	float textureU = (frame % framesHorizontal) * frameStepX;
	float textureV = floor(frame / framesHorizontal) * frameStepY;
	v_textureUVs = textureUVs + vec2(textureU, textureV);
}
