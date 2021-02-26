#version 330 core

in vec4 v_vertexColour;
in vec2 v_texCoord;

out vec4 fragColour;

uniform sampler2D u_texture;

void main() {
	vec4 texColour = texture(u_texture, v_texCoord);
	fragColour = texColour * v_vertexColour;
}
