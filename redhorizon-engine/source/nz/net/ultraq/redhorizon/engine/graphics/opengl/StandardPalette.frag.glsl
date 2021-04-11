#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;
in float v_textureUnit;

out vec4 fragmentColour;

uniform sampler2D textures[maxTextureUnits];
uniform sampler1D palette;

void main() {
	vec4 index = texture(textures[int(v_textureUnit)], v_textureUVs);
	vec4 textureColour = texture(palette, index.r);
	fragmentColour = textureColour * v_vertexColour;
}
