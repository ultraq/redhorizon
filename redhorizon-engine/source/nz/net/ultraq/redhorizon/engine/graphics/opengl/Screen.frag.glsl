#version 410 core

in vec4 v_vertexColour;
in vec2 v_textureUVs;
in float v_textureUnit;

out vec4 fragmentColour;

uniform sampler2D textures[MAX_TEXTURE_UNITS];

/**
 * Fragment shader main function, emits the fragment colour for a texture in a
 * given texture unit.
 */
void main() {

	fragmentColour = texture(textures[int(v_textureUnit)], v_textureUVs);
	fragmentColour *= v_vertexColour;
}
