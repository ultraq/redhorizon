#version 410 core

in vec4 v_vertexColour;

out vec4 fragColour;

void main() {
	fragColour = v_vertexColour;
}
