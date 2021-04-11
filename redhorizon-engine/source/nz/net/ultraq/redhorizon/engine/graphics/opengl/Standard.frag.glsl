#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;
in float v_textureUnit;

out vec4 fragmentColour;

uniform sampler2D textures[maxTextureUnits];

void main() {
	vec4 textureColour = texture(textures[int(v_textureUnit)], v_textureUVs);
	fragmentColour = textureColour * v_vertexColour;
}
