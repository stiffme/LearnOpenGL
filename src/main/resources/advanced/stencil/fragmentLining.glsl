#version 330 core

uniform sampler2D texuture1;

out vec4 oColor;
in vec2 texCoord;
void main() {
    oColor = vec4(0.04, 0.28, 0.26, 1.0);
}
