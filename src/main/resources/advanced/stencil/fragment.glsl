#version 330 core

uniform sampler2D texuture1;

out vec4 oColor;
in vec2 texCoord;
void main() {
    oColor = texture(texuture1, texCoord);
}
