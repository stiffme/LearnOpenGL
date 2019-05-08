#version 330 core

layout (location=0) in vec3 position;
layout (location=1) in vec3 color;
uniform vec3 offset;
out vec3 outColor;
void main() {
    gl_Position = vec4(position + offset, 1.0f);
    outColor = position;
}
