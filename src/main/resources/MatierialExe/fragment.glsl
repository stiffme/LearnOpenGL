#version 330 core

in vec3 oNormal; //normal direction in view space
in vec3 oFragCoord; //frag coord in view space

out vec4 oColor;

void main() {
    oColor = vec4(1.0f);
}
