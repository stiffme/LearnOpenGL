#version 330 core
layout (location=0) in vec3 aPosition;
layout (location=1) in vec2 aTextureCoord;

uniform mat4 projection;
uniform mat4 model;
uniform mat4 view;
out vec2 outTextureCoord;

void main() {
    gl_Position = projection * view * model * vec4(aPosition, 1.0f);
    outTextureCoord = aTextureCoord;
}
