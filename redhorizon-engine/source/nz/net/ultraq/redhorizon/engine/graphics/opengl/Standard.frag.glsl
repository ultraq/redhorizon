#version 410 core

in vec4 v_vertexColour;
in vec2 v_texCoord;
in float v_texUnit;
in float v_texLayer;

out vec4 fragColour;

uniform sampler2DArray u_textures[maxTextureUnits];

void main() {
	vec4 texColour = texture(u_textures[int(v_texUnit)], vec3(v_texCoord.xy, v_texLayer));
	fragColour = texColour * v_vertexColour;
}
