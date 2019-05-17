#version 330 core

uniform sampler2D diffuse0;
uniform sampler2D diffuse1;

in vec2 oTexCoord;
out vec4 oColor;
void main() {
    oColor = texture(diffuse0, oTexCoord);
}
