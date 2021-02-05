#version 330 core

in vec2 v_texCoord;

out vec4 colour;

uniform sampler2D u_texture;

void main() {
	vec4 texColour = texture(u_texture, v_texCoord);
	colour = texColour;
}
